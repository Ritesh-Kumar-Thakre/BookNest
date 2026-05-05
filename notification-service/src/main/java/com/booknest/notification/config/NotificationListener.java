package com.booknest.notification.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.booknest.notification.dto.request.CreateNotificationRequest;
import com.booknest.notification.entity.NotificationType;
import com.booknest.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

/**
 * Listens for notification messages from RabbitMQ and processes them.
 */
@Component
@RequiredArgsConstructor
public class NotificationListener {

	private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
	private final NotificationService notificationService;

	@SuppressWarnings("unchecked")
	@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
	public void handleNotification(Map<String, Object> message) {
		try {
			log.info("Received notification from RabbitMQ: {}", message);

			Integer userId = message.get("userId") instanceof Number
					? ((Number) message.get("userId")).intValue()
					: Integer.parseInt(message.get("userId").toString());

			String typeStr = (String) message.get("type");
			Map<String, String> params = (Map<String, String>) message.get("params");

			CreateNotificationRequest request = new CreateNotificationRequest();
			request.setUserId(userId);
			request.setType(NotificationType.valueOf(typeStr));
			request.setParams(params);

			notificationService.create(request);
			log.info("Notification processed successfully: userId={}, type={}", userId, typeStr);
		} catch (Exception e) {
			log.error("Failed to process notification from RabbitMQ: {}", e.getMessage(), e);
		}
	}
}
