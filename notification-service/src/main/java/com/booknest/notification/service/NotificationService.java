package com.booknest.notification.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.booknest.notification.dto.request.CreateNotificationRequest;
import com.booknest.notification.dto.response.NotificationResponse;

public interface NotificationService {
	NotificationResponse create(CreateNotificationRequest request);
	Page<NotificationResponse> getByUser(Integer userId, Pageable pageable);
	long getUnreadCount(Integer userId);
	void markAsRead(Integer notificationId);
	void markAllAsRead(Integer userId);
	void delete(Integer notificationId);
}
