package com.cg.order.security.util;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {

	private final String SECRET = "mysecretkey";

	public boolean validateToken(String token) {

		try {

			Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);

			return true;

		} catch (Exception e) {

			return false;

		}

	}

}