package com.booknest.notification.dto.response;

import java.time.LocalDateTime;

import com.booknest.notification.entity.Notification;
import com.booknest.notification.entity.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
	private Integer id;
	private Integer userId;
	private String title;
	private String message;
	private NotificationType type;
	private Boolean isRead;
	private LocalDateTime createdAt;

	public static NotificationResponse from(Notification n) {
		return NotificationResponse.builder()
				.id(n.getId())
				.userId(n.getUserId())
				.title(n.getTitle())
				.message(n.getMessage())
				.type(n.getType())
				.isRead(n.getIsRead())
				.createdAt(n.getCreatedAt())
				.build();
	}
}
