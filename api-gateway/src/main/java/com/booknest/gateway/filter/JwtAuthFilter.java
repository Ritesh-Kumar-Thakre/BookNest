package com.booknest.gateway.filter;

import com.booknest.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

	@Autowired
	private JwtUtil jwtUtil;

	// Paths that don't require authentication
	private static final List<String> OPEN_ENDPOINTS = List.of(
			"/api/v1/auth/register",
			"/api/v1/auth/login",
			"/api/v1/auth/logout",
			"/api/v1/books",
			"/api/v1/reviews/book/",
			"/api/v1/reviews/average/",
			"/actuator"
	);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();
		String method = request.getMethod().name();

		boolean isOpen = isOpenEndpoint(path, method);

		// Check for Authorization header
		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;

		if (token != null) {
			boolean isValid = jwtUtil.validateToken(token);
			
			if (isValid) {
				// Valid token: extract user info and inject headers
				Integer userId = jwtUtil.extractUserId(token);
				String role = jwtUtil.extractRole(token);
				String email = jwtUtil.extractSubject(token);

				ServerHttpRequest.Builder builder = request.mutate();
				if (userId != null) {
					builder.header("X-User-Id", userId.toString());
				}
				builder.header("X-User-Role", role != null ? role : "CUSTOMER")
					   .header("X-User-Email", email != null ? email : "");

				// IMPORTANT: Strip Authorization header for downstream services 
				// to prevent them from trying to re-validate the token.
				if (!path.contains("/auth/")) {
					builder.headers(h -> h.remove(HttpHeaders.AUTHORIZATION));
				}

				return chain.filter(exchange.mutate().request(builder.build()).build());
			} else {
				// Invalid token
				if (isOpen) {
					// Open endpoint: proceed but strip the invalid Authorization header
					ServerHttpRequest modifiedRequest = request.mutate()
							.headers(httpHeaders -> httpHeaders.remove(HttpHeaders.AUTHORIZATION))
							.build();
					return chain.filter(exchange.mutate().request(modifiedRequest).build());
				} else {
					// Secured endpoint: 401
					return onError(exchange, HttpStatus.UNAUTHORIZED);
				}
			}
		} else {
			// No token
			if (isOpen) {
				return chain.filter(exchange);
			} else {
				return onError(exchange, HttpStatus.UNAUTHORIZED);
			}
		}
	}

	private boolean isOpenEndpoint(String path, String method) {
		if ("OPTIONS".equals(method)) return true;
		
		String p = path.toLowerCase();
		
		// All actuator endpoints are open
		if (p.contains("/actuator")) return true;

		// Static file uploads are always public (book images, profile images)
		if (p.contains("/uploads/")) return true;

		// Auth endpoints that should be accessible without a VALID token (invalid token will be stripped)
		if (p.contains("/auth/register") || 
			p.contains("/auth/login") || 
			p.contains("/auth/google") ||
			p.contains("/auth/logout") ||
			p.contains("/auth/me") ||
			p.contains("/auth/refresh") ||
			p.contains("/auth/getprofile")) {
			return true;
		}

		// Public book and review endpoints are open
		if (p.contains("/books/all") || 
			p.contains("/books/featured") || 
			p.contains("/books/search") || 
			p.contains("/books/genre") ||
			p.contains("/books/details/") ||
			p.contains("/books/images/") ||
			p.contains("/reviews/book/") ||
			p.contains("/reviews/average/")) {
			return true;
		}
		
		// Fallback for general book browsing (single book by ID)
		if (p.contains("/books") && !p.contains("/pending") && !p.contains("/addbook") && !p.contains("/delete") && !p.contains("/upload") && !p.contains("/update") && !p.contains("/verify")) {
			return true;
		}

		return false;
	}

	private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().add("X-Gateway-Error", "Access Denied");
		return response.setComplete();
	}

	@Override
	public int getOrder() {
		return -1; // Run before other filters
	}
}
