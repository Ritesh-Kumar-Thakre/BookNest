package com.booknest.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-Service")
public interface AuthServiceClient {

	@GetMapping("/auth/internal/user/{userId}/email")
	String getEmailByUserId(@PathVariable Integer userId);
}
