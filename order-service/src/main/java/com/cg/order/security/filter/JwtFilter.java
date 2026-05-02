package com.cg.order.security.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cg.order.security.util.JwtUtil;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//
//		String authHeader = request.getHeader("Authorization");
//
//		if (authHeader != null && authHeader.startsWith("Bearer ")) {
//
//			String token = authHeader.substring(7);
//
//			if (!jwtUtil.validateToken(token)) {
//				response.setStatus(401);
//				return;
//			}
//		} else {
//
//			response.setStatus(401);
//			return;
//		}
//
//		filterChain.doFilter(request, response);
//	}
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		filterChain.doFilter(request, response);

	}
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//
//		String path = request.getRequestURI();
//
//		if (path.startsWith("/cart/")) {
//
//			filterChain.doFilter(request, response);
//
//			return;
//		}
//
//		String authHeader = request.getHeader("Authorization");
//
//		if (authHeader != null && authHeader.startsWith("Bearer ")) {
//
//			String token = authHeader.substring(7);
//
//			if (!jwtUtil.validateToken(token)) {
//				response.setStatus(401);
//				return;
//			}
//
//		} else {
//
//			response.setStatus(401);
//			return;
//		}
//
//		filterChain.doFilter(request, response);
//	}
}
