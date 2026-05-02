package com.cg.order.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

	@PostMapping("/notifications")
	Map<String, Object> createNotification(@RequestBody Map<String, Object> request);
}
