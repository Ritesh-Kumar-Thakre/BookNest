package com.booknest.auth.service;

import java.util.List;
import java.util.Optional;

import com.booknest.auth.dto.response.AuthResponse;
import com.booknest.auth.dto.response.UserResponse;
import com.booknest.user.entity.User;

public interface AuthService {
	AuthResponse register(User user);

	String login(String email, String password);

	void logout(String token);

	boolean validateToken(String token);

	String refreshToken(String refreshToken);

	Optional<User> getUserByEmail(String email);

	void changePassword(Integer userId, String oldPassword, String newPassword);

	UserResponse getProfile(Integer userID);

	User update(User user);

	void deleteAccount(Integer userId);

	List<UserResponse> getAllUsers();

	void changeRole(Integer userId, String role);

}
