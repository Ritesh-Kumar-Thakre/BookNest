package com.booknest.notification.dto.request;

import java.util.Map;

import com.booknest.notification.entity.NotificationType;

import lombok.Data;

@Data
public class CreateNotificationRequest {
	private Integer userId;
	private NotificationType type;
	private Map<String, String> params; // e.g. {"orderId": "42", "amount": "599.00"}
}
