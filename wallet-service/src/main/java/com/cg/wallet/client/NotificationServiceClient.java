package com.cg.wallet.client;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cg.wallet.dto.request.NotificationRequest;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

	@PostMapping("/notifications")
	void sendNotification(@RequestBody NotificationRequest request);

}