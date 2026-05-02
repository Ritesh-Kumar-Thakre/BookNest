package com.booknest.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications", indexes = {
		@Index(name = "idx_notif_user", columnList = "userId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer userId;

	private String title;

	@Column(length = 1000)
	private String message;

	@Enumerated(EnumType.STRING)
	private NotificationType type;

	@Builder.Default
	private Boolean isRead = false;

	@Column(updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
