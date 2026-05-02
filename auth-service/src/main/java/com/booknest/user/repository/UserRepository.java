package com.booknest.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booknest.user.entity.Role;
import com.booknest.user.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByEmail(String s);

	User findByUserId(Integer id);

	boolean existsByEmail(String email);

	List<User> findAllByRole(String role);

	void deleteByUserId(Integer userId);
	
}