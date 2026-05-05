package com.booknest.auth.service;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.booknest.auth.dto.response.AuthResponse;
import com.booknest.auth.util.JwtUtil;
import com.booknest.user.entity.Role;
import com.booknest.user.entity.User;
import com.booknest.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

	private static final Logger log = LoggerFactory.getLogger(GoogleOAuthService.class);

	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;

	@Value("${google.client.id}")
	private String googleClientId;

	/**
	 * Verifies Google ID token and either logs in existing user or registers a new one.
	 * Returns a JWT token for the BookNest application.
	 */
	public AuthResponse loginWithGoogle(String idTokenString) {
		try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
					new NetHttpTransport(), GsonFactory.getDefaultInstance())
					.setAudience(Collections.singletonList(googleClientId))
					.build();

			GoogleIdToken idToken = verifier.verify(idTokenString);
			if (idToken == null) {
				throw new RuntimeException("Invalid Google token");
			}

			GoogleIdToken.Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			String name = (String) payload.get("name");
			String pictureUrl = (String) payload.get("picture");

			log.info("Google OAuth login: email={}, name={}", email, name);

			// Check if user exists
			var existingUser = userRepository.findByEmail(email);
			if (existingUser.isPresent()) {
				// Existing user — generate JWT and return
				User user = existingUser.get();
				// Update profile image from Google if not already set
				if (user.getProfileImageUrl() == null && pictureUrl != null) {
					user.setProfileImageUrl(pictureUrl);
					userRepository.save(user);
				}
				String token = jwtUtil.generateToken(user.getEmail(), user.getUserId(), user.getRole().name());
				return new AuthResponse(token, "Google Login Successful");
			} else {
				// New user — register with Google data
				User newUser = new User();
				newUser.setEmail(email);
				newUser.setFullName(name != null ? name : "Google User");
				newUser.setPassword(passwordEncoder.encode("GOOGLE_OAUTH_" + System.currentTimeMillis()));
				newUser.setRole(Role.CUSTOMER);
				newUser.setProvider("google");
				newUser.setMobile(0L); // Will need to update later
				if (pictureUrl != null) {
					newUser.setProfileImageUrl(pictureUrl);
				}

				newUser = userRepository.save(newUser);
				log.info("New Google user registered: id={}, email={}", newUser.getUserId(), email);

				String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getUserId(), newUser.getRole().name());
				return new AuthResponse(token, "Google Registration Successful");
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			log.error("Google OAuth verification failed: {}", e.getMessage());
			throw new RuntimeException("Google authentication failed: " + e.getMessage());
		}
	}
}
