package com.booknest.auth.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.booknest.auth.dto.response.AuthResponse;
import com.booknest.auth.dto.response.UserResponse;
import com.booknest.auth.service.AuthService;
import com.booknest.auth.util.JwtUtil;
import com.booknest.user.entity.Role;
import com.booknest.user.entity.User;
import com.booknest.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;

	@Override
	public AuthResponse register(User user) {

//		Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
		Optional<User> existingUser = getUserByEmail(user.getEmail());

		if (existingUser.isPresent()) {
			throw new RuntimeException("User already exists");
		}

		// hash password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// default values if not provided by client
		if (user.getRole() == null) {
			user.setRole(Role.CUSTOMER);
		}

		user.setProvider("local");

		User savedUser = userRepository.save(user);
		log.info("User registered: id={}, email={}, role={}", savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole());

		// generate JWT
		String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getUserId(), savedUser.getRole().name());

		return new AuthResponse(token, "Registration Success");
	}

	@Override
	public String login(String email, String password) {

//		Optional<User> user = userRepository.findByEmail(email);
		Optional<User> user = getUserByEmail(email);

		if (user.isPresent()) {

			if (passwordEncoder.matches(password, // raw input
					user.get().getPassword() // stored hash
			)) {

				return jwtUtil.generateToken(user.get().getEmail(), user.get().getUserId(), user.get().getRole().name());
			}
		}
		log.warn("Login failed for email={}", email);

		throw new RuntimeException("Invalid Credentials");
	}

	@Override
	public void logout(String token) {
		// handle in client side --frontend
	}

	@Override
	public boolean validateToken(String token) {
		if (token == null || token.isEmpty()) {
			return false;
		}
		try {
			return jwtUtil.validateToken(token);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String refreshToken(String refreshToken) {
		if (refreshToken == null || refreshToken.isEmpty()) {
			throw new RuntimeException("Invalid Token");
		}
		if (jwtUtil.validateToken(refreshToken)) {
			String email = jwtUtil.extractUsername(refreshToken);
			Integer userId = jwtUtil.extractUserId(refreshToken);
			String role = jwtUtil.extractRole(refreshToken);
			return jwtUtil.generateToken(email, userId, role);
		}
		throw new RuntimeException("Refresh Token Expired");
	}

	@Override
	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void changePassword(Integer userId, String oldPassword, String newPassword) {
		Optional<User> user = userRepository.findById(userId);
		if (!user.isPresent()) {
			throw new RuntimeException("Invalid User");
		}

		if (!passwordEncoder.matches(oldPassword, user.get().getPassword())) {
			throw new RuntimeException("Invalid Password");
		}

		user.get().setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user.get());
	}

	@Override
	public UserResponse getProfile(Integer userID) {
		Optional<User> user = userRepository.findById(userID);
		if (!user.isPresent()) {
			throw new RuntimeException("User Not Found");
		}
		return new UserResponse(user.get());
	}

	@Override
	public User update(User user) {

		Optional<User> user1 = userRepository.findById(user.getUserId());

		if (!user1.isPresent()) {
			throw new RuntimeException("User Not Found");
		}

		User updateUser = user1.get();
		if (user.getFullName() != null) {
			updateUser.setFullName(user.getFullName());
		}
		if (user.getMobile() != null) {
			updateUser.setMobile(user.getMobile());
		}
		if (user.getProfileImageUrl() != null) {
			updateUser.setProfileImageUrl(user.getProfileImageUrl());
		}

		return userRepository.save(updateUser);
	}

	@Override
	public void deleteAccount(Integer userId) {
		if (userRepository.findById(userId).isPresent()) {
			userRepository.deleteById(userId);
		} else {
			throw new RuntimeException("User not exist's");
		}
	}

	@Override
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
				.map(UserResponse::new)
				.toList();
	}

	@Override
	public void changeRole(Integer userId, String role) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		try {
			Role newRole = Role.valueOf(role.toUpperCase());
			user.setRole(newRole);
			userRepository.save(user);
			log.info("User role updated: userId={}, email={}, newRole={}", userId, user.getEmail(), newRole);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid role: " + role + ". Available roles: CUSTOMER, SELLER, ADMIN");
		}
	}
}