package com.booknest.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.booknest.auth.dto.response.AuthResponse;
import com.booknest.auth.dto.response.UserResponse;
import com.booknest.auth.service.impl.AuthServiceImpl;
import com.booknest.auth.util.JwtUtil;
import com.booknest.user.entity.Role;
import com.booknest.user.entity.User;
import com.booknest.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthServiceImpl authService;

	private User sampleUser;

	@BeforeEach
	void setUp() {
		sampleUser = new User();
		sampleUser.setUserId(1);
		sampleUser.setFullName("John Doe");
		sampleUser.setEmail("john@example.com");
		sampleUser.setPassword("hashedPassword");
		sampleUser.setRole(Role.CUSTOMER);
		sampleUser.setMobile(9876543210L);
		sampleUser.setProvider("local");
	}

	// ──────────────────────────────────────────────
	// register
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("register()")
	class RegisterTests {

		@Test
		@DisplayName("should register new user successfully")
		void register_Success() {
			when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
			when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
			when(userRepository.save(any(User.class))).thenReturn(sampleUser);
			when(jwtUtil.generateToken(anyString(), anyInt(), anyString())).thenReturn("jwt-token");

			AuthResponse response = authService.register(sampleUser);

			assertNotNull(response);
			assertEquals("jwt-token", response.getToken());
			assertEquals("Registration Success", response.getMessage());
			verify(userRepository).save(any(User.class));
		}

		@Test
		@DisplayName("should throw when user already exists")
		void register_UserExists_ShouldThrow() {
			when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));

			RuntimeException ex = assertThrows(RuntimeException.class,
					() -> authService.register(sampleUser));
			assertEquals("User already exists", ex.getMessage());
			verify(userRepository, never()).save(any());
		}

		@Test
		@DisplayName("should default role to CUSTOMER when null")
		void register_NullRole_ShouldDefaultCustomer() {
			sampleUser.setRole(null);
			when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
			when(passwordEncoder.encode(anyString())).thenReturn("encoded");
			when(userRepository.save(any(User.class))).thenReturn(sampleUser);
			when(jwtUtil.generateToken(anyString(), anyInt(), anyString())).thenReturn("token");

			authService.register(sampleUser);

			assertEquals(Role.CUSTOMER, sampleUser.getRole());
		}
	}

	// ──────────────────────────────────────────────
	// login
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("login()")
	class LoginTests {

		@Test
		@DisplayName("should login with valid credentials")
		void login_Success() {
			when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
			when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);
			when(jwtUtil.generateToken(anyString(), anyInt(), anyString())).thenReturn("jwt-token");

			String token = authService.login("john@example.com", "rawPassword");

			assertEquals("jwt-token", token);
		}

		@Test
		@DisplayName("should throw on invalid password")
		void login_InvalidPassword_ShouldThrow() {
			when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));
			when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

			assertThrows(RuntimeException.class, () -> authService.login("john@example.com", "wrongPassword"));
		}

		@Test
		@DisplayName("should throw on non-existent email")
		void login_UserNotFound_ShouldThrow() {
			when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

			assertThrows(RuntimeException.class, () -> authService.login("unknown@test.com", "anyPass"));
		}
	}

	// ──────────────────────────────────────────────
	// validateToken
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("validateToken()")
	class ValidateTokenTests {

		@Test
		@DisplayName("should return true for valid token")
		void validateToken_Valid() {
			when(jwtUtil.validateToken("valid-token")).thenReturn(true);

			assertTrue(authService.validateToken("valid-token"));
		}

		@Test
		@DisplayName("should return false for null token")
		void validateToken_Null() {
			assertFalse(authService.validateToken(null));
		}

		@Test
		@DisplayName("should return false for empty token")
		void validateToken_Empty() {
			assertFalse(authService.validateToken(""));
		}

		@Test
		@DisplayName("should return false on exception")
		void validateToken_Exception() {
			when(jwtUtil.validateToken(anyString())).thenThrow(new RuntimeException("Bad token"));

			assertFalse(authService.validateToken("bad-token"));
		}
	}

	// ──────────────────────────────────────────────
	// getProfile
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("getProfile should return user response")
	void getProfile_Success() {
		when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

		UserResponse profile = authService.getProfile(1);

		assertNotNull(profile);
		assertEquals("John Doe", profile.getFullName());
		assertEquals("john@example.com", profile.getEmail());
	}

	@Test
	@DisplayName("getProfile should throw when user not found")
	void getProfile_NotFound_ShouldThrow() {
		when(userRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> authService.getProfile(99));
	}

	// ──────────────────────────────────────────────
	// changePassword
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("changePassword should succeed with correct old password")
	void changePassword_Success() {
		when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
		when(passwordEncoder.matches("oldPass", "hashedPassword")).thenReturn(true);
		when(passwordEncoder.encode("newPass")).thenReturn("newHashedPassword");

		authService.changePassword(1, "oldPass", "newPass");

		verify(userRepository).save(sampleUser);
	}

	@Test
	@DisplayName("changePassword should throw on wrong old password")
	void changePassword_WrongOld_ShouldThrow() {
		when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
		when(passwordEncoder.matches("wrongOld", "hashedPassword")).thenReturn(false);

		assertThrows(RuntimeException.class, () -> authService.changePassword(1, "wrongOld", "newPass"));
	}

	// ──────────────────────────────────────────────
	// update
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("update should modify user fields")
	void update_Success() {
		User updateData = new User();
		updateData.setUserId(1);
		updateData.setFullName("John Updated");

		when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
		when(userRepository.save(any(User.class))).thenReturn(sampleUser);

		User result = authService.update(updateData);

		assertNotNull(result);
		verify(userRepository).save(any(User.class));
	}

	// ──────────────────────────────────────────────
	// deleteAccount
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("deleteAccount should delete existing user")
	void deleteAccount_Success() {
		when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
		doNothing().when(userRepository).deleteById(1);

		authService.deleteAccount(1);

		verify(userRepository).deleteById(1);
	}

	@Test
	@DisplayName("deleteAccount should throw when user not found")
	void deleteAccount_NotFound_ShouldThrow() {
		when(userRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> authService.deleteAccount(99));
	}

	// ──────────────────────────────────────────────
	// getAllUsers
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("getAllUsers should return list of user responses")
	void getAllUsers() {
		when(userRepository.findAll()).thenReturn(List.of(sampleUser));

		List<UserResponse> result = authService.getAllUsers();

		assertEquals(1, result.size());
		assertEquals("John Doe", result.get(0).getFullName());
	}

	// ──────────────────────────────────────────────
	// changeRole
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("changeRole should update user role")
	void changeRole_Success() {
		when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
		when(userRepository.save(any(User.class))).thenReturn(sampleUser);

		authService.changeRole(1, "SELLER");

		assertEquals(Role.SELLER, sampleUser.getRole());
	}

	@Test
	@DisplayName("changeRole should throw for invalid role")
	void changeRole_InvalidRole_ShouldThrow() {
		when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

		assertThrows(RuntimeException.class, () -> authService.changeRole(1, "INVALID"));
	}
}
