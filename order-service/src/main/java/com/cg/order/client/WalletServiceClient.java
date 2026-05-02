package com.cg.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "wallet-service")
public interface WalletServiceClient {

	@GetMapping("/wallet/balance")
	Map<String, Double> getBalance(@RequestHeader("X-User-Id") Integer userId);

	@PutMapping("/wallet/pay")
	Map<String, Boolean> payMoney(@RequestHeader("X-User-Id") Integer userId, @RequestParam Double amount, @RequestParam Integer orderId);

	@PutMapping("/wallet/refund")
	Map<String, Boolean> refundMoney(@RequestHeader("X-User-Id") Integer userId, @RequestParam Double amount, @RequestParam Integer orderId);

}