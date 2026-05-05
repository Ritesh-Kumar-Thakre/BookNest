package com.cg.wallet.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cg.wallet.entity.Wallet;
import com.cg.wallet.repository.WalletRepository;
import com.cg.wallet.service.RazorpayService;
import com.cg.wallet.service.WalletService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl implements RazorpayService {

	private static final Logger log = LoggerFactory.getLogger(RazorpayServiceImpl.class);

	private final RazorpayClient razorpayClient;
	private final WalletRepository walletRepository;
	private final WalletService walletService;

	@Value("${razorpay.key.id}")
	private String razorpayKeyId;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	@Override
	public Map<String, Object> createOrder(Double amount) throws Exception {
		JSONObject options = new JSONObject();
		// Razorpay expects amount in paise (1 INR = 100 paise)
		options.put("amount", (int) (amount * 100));
		options.put("currency", "INR");
		options.put("receipt", "wallet_topup_" + System.currentTimeMillis());
		options.put("payment_capture", 1); // auto-capture

		Order order = razorpayClient.orders.create(options);

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("orderId", order.get("id"));
		response.put("amount", order.get("amount"));
		response.put("currency", order.get("currency"));
		response.put("key", razorpayKeyId);
		return response;
	}

	@Override
	public void verifyAndCredit(Integer walletId, String razorpayOrderId,
			String razorpayPaymentId, String razorpaySignature) throws Exception {

		// 1. Verify the signature
		String payload = razorpayOrderId + "|" + razorpayPaymentId;
		String expectedSignature = hmacSha256(payload, razorpayKeySecret);

		if (!expectedSignature.equals(razorpaySignature)) {
			throw new RuntimeException("Payment verification failed — invalid signature");
		}

		// 2. Fetch order to get the actual amount
		Order order = razorpayClient.orders.fetch(razorpayOrderId);
		int amountInPaise = order.get("amount");
		double amountInRupees = amountInPaise / 100.0;

		// 3. Credit the wallet
		Wallet wallet = walletRepository.findByWalletId(walletId)
				.orElseThrow(() -> new RuntimeException("Wallet not found"));

		walletService.addMoney(wallet, amountInRupees, "Razorpay TopUp (" + razorpayPaymentId + ")");

		log.info("Razorpay payment verified & credited: walletId={}, amount={}, paymentId={}",
				walletId, amountInRupees, razorpayPaymentId);
	}

	@Override
	public boolean verifySignatureOnly(String razorpayOrderId,
			String razorpayPaymentId, String razorpaySignature) throws Exception {
		String payload = razorpayOrderId + "|" + razorpayPaymentId;
		String expectedSignature = hmacSha256(payload, razorpayKeySecret);

		if (!expectedSignature.equals(razorpaySignature)) {
			throw new RuntimeException("Payment verification failed — invalid signature");
		}

		log.info("Razorpay signature verified (order payment): orderId={}, paymentId={}",
				razorpayOrderId, razorpayPaymentId);
		return true;
	}

	/**
	 * Computes HMAC-SHA256 hex digest for Razorpay signature verification.
	 */
	private String hmacSha256(String data, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
		byte[] hash = mac.doFinal(data.getBytes("UTF-8"));
		StringBuilder hex = new StringBuilder();
		for (byte b : hash) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}
}
