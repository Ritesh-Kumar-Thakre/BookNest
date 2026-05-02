package com.booknest.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

	@Value("${jwt.secret}")
	private String jwtSecret;

	public Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public boolean validateToken(String token) {
		try {
			extractAllClaims(token);
			return true;
		} catch (Exception e) {
			log.warn("JWT validation error in gateway: {}", e.getMessage());
			return false;
		}
	}

	public String extractSubject(String token) {
		return extractAllClaims(token).getSubject();
	}

	public Integer extractUserId(String token) {
		Claims claims = extractAllClaims(token);
		Object userId = claims.get("userId");
		if (userId == null) return null;
		
		if (userId instanceof Integer) return (Integer) userId;
		if (userId instanceof Number) return ((Number) userId).intValue();
		
		try {
			return Integer.parseInt(userId.toString());
		} catch (NumberFormatException e) {
			// Handle cases like "1.0"
			try {
				return (int) Double.parseDouble(userId.toString());
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public String extractRole(String token) {
		Claims claims = extractAllClaims(token);
		Object role = claims.get("role");
		return role != null ? role.toString() : "CUSTOMER";
	}
}
