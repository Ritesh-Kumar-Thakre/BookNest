package com.cg.wallet.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cg.wallet.service.RazorpayService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wallet/razorpay")
@RequiredArgsConstructor
public class RazorpayController {

	private final RazorpayService razorpayService;

	/**
	 * Creates a Razorpay order for the given top-up amount.
	 * POST /wallet/razorpay/create-order?amount=500
	 */
	@PostMapping("/create-order")
	public ResponseEntity<?> createOrder(@RequestParam Double amount) {
		try {
			if (amount == null || amount < 1) {
				return ResponseEntity.badRequest()
						.body(Map.of("message", "Amount must be at least ₹1"));
			}
			Map<String, Object> order = razorpayService.createOrder(amount);
			return ResponseEntity.ok(order);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to create Razorpay order: " + e.getMessage()));
		}
	}

	/**
	 * Verifies the Razorpay payment and credits the wallet.
	 * POST /wallet/razorpay/verify
	 */
	@PostMapping("/verify")
	public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
		try {
			Integer walletId = Integer.parseInt(payload.get("walletId"));
			String orderId = payload.get("razorpayOrderId");
			String paymentId = payload.get("razorpayPaymentId");
			String signature = payload.get("razorpaySignature");

			razorpayService.verifyAndCredit(walletId, orderId, paymentId, signature);

			return ResponseEntity.ok(Map.of("message", "Payment verified and wallet credited"));
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest()
					.body(Map.of("message", "Invalid wallet ID"));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Verification failed: " + e.getMessage()));
		}
	}

	/**
	 * Verifies Razorpay signature only (no wallet credit).
	 * Used for order checkout payments via Razorpay.
	 * POST /wallet/razorpay/verify-signature
	 */
	@PostMapping("/verify-signature")
	public ResponseEntity<?> verifySignatureOnly(@RequestBody Map<String, String> payload) {
		try {
			String orderId = payload.get("razorpayOrderId");
			String paymentId = payload.get("razorpayPaymentId");
			String signature = payload.get("razorpaySignature");

			razorpayService.verifySignatureOnly(orderId, paymentId, signature);

			return ResponseEntity.ok(Map.of("verified", true, "message", "Payment signature verified"));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("verified", false, "message", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("verified", false, "message", "Verification failed: " + e.getMessage()));
		}
	}
}
