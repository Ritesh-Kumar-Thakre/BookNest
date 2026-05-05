package com.cg.order.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Publishes notification messages to RabbitMQ.
 * The notification-service will consume these messages asynchronously.
 */
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

	private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
	private final RabbitTemplate rabbitTemplate;

	/**
	 * Sends a notification message to RabbitMQ.
	 */
	public void sendNotification(Integer userId, String type, Integer orderId, Double amount, Boolean isRefunded) {
		try {
			Map<String, Object> message = new HashMap<>();
			message.put("userId", userId);
			message.put("type", type);

			Map<String, String> params = new HashMap<>();
			if (orderId != null) params.put("orderId", String.valueOf(orderId));
			if (amount != null) params.put("amount", String.valueOf(amount));
			if (isRefunded != null) params.put("isRefunded", String.valueOf(isRefunded));
			message.put("params", params);

			rabbitTemplate.convertAndSend(
					RabbitMQConfig.NOTIFICATION_EXCHANGE,
					"notification.order",
					message
			);
			log.info("Notification published to RabbitMQ: userId={}, type={}, orderId={}", userId, type, orderId);
		} catch (Exception e) {
			log.error("Failed to publish notification to RabbitMQ: userId={}, type={}, error={}", userId, type, e.getMessage());
		}
	}
}
