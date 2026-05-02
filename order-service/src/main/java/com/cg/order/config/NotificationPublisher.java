package com.cg.order.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

/**
 * Sends notifications to the notification-service using direct REST calls.
 * Uses Eureka DiscoveryClient to resolve the notification-service URL.
 * Falls back to localhost:8088 if service is not found in Eureka.
 */
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

	private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
	private final DiscoveryClient discoveryClient;
	private final RestTemplate restTemplate;

	/**
	 * Sends a notification by calling the notification-service REST API directly.
	 */
	public void sendNotification(Integer userId, String type, Integer orderId, Double amount, Boolean isRefunded) {
		try {
			// Build the request body matching CreateNotificationRequest DTO
			Map<String, Object> request = new HashMap<>();
			request.put("userId", userId);
			request.put("type", type);

			Map<String, String> params = new HashMap<>();
			if (orderId != null) params.put("orderId", String.valueOf(orderId));
			if (amount != null) params.put("amount", String.valueOf(amount));
			if (isRefunded != null) params.put("isRefunded", String.valueOf(isRefunded));
			request.put("params", params);

			// Resolve notification-service URL via Eureka
			String baseUrl = resolveServiceUrl("notification-service");
			String url = baseUrl + "/notifications";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

			restTemplate.postForEntity(url, entity, String.class);
			log.info("Notification sent successfully to {}: userId={}, type={}, orderId={}", url, userId, type, orderId);
		} catch (Exception e) {
			log.error("Failed to send notification to notification-service: userId={}, type={}, error={}", userId, type, e.getMessage());
		}
	}

	/**
	 * Resolves service URL from Eureka, falls back to localhost if not found.
	 */
	private String resolveServiceUrl(String serviceName) {
		try {
			var instances = discoveryClient.getInstances(serviceName);
			if (instances != null && !instances.isEmpty()) {
				ServiceInstance instance = instances.get(0);
				String url = instance.getUri().toString();
				log.debug("Resolved {} to {}", serviceName, url);
				return url;
			}
		} catch (Exception e) {
			log.warn("Failed to resolve {} from Eureka: {}", serviceName, e.getMessage());
		}
		// Fallback to known port
		log.warn("Using fallback URL for {}: http://localhost:8088", serviceName);
		return "http://localhost:8088";
	}
}
