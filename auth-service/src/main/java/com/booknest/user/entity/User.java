package com.booknest.user.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;

	@Column(name = "Full_Name", nullable = false)
	private String fullName;

	@Column(name = "Email", nullable = false)
	private String email;

	@Column(name="pass_word_hash", nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)// DB stores the text value rather then number 0,1 etc for roles
	@Column(name = "role", length = 20)
	private Role role;

	private String provider;

	@Column(name = "Mobile", nullable = false)
	private Long mobile;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "Created_Time", updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

}
