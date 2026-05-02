package com.booknest.auth.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;

@Data
@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private long tokenExpire;

	public String generateToken(String email, Integer userId, String role) {
		return Jwts.builder()
				.setSubject(email)
				.claim("userId", userId)
				.claim("role", role)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + tokenExpire))
				.signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
				.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build().parseClaimsJws(token);
			return !isTokenExpired(token);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isTokenExpired(String token) {
		Date expiry = extractExpiration(token);
		return expiry.before(new Date());
	}

	public Date extractExpiration(String token) {
		Claims claims = extractAllClaims(token);
		return claims.getExpiration();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public Integer extractUserId(String token) {
		Object userId = extractAllClaims(token).get("userId");
		return userId != null ? Integer.parseInt(userId.toString()) : null;
	}

	public String extractRole(String token) {
		Object role = extractAllClaims(token).get("role");
		return role != null ? role.toString() : "CUSTOMER";
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}
