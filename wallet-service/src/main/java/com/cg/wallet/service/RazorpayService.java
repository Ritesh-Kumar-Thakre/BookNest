package com.cg.wallet.service;

import java.util.Map;

public interface RazorpayService {

	/**
	 * Creates a Razorpay order for the given amount (in INR).
	 * Returns orderId, amount, currency, and key.
	 */
	Map<String, Object> createOrder(Double amount) throws Exception;

	/**
	 * Verifies the Razorpay payment signature and credits the wallet.
	 */
	void verifyAndCredit(Integer walletId, String razorpayOrderId,
			String razorpayPaymentId, String razorpaySignature) throws Exception;

	/**
	 * Verifies only the Razorpay payment signature (no wallet credit).
	 * Used for order checkout payments.
	 */
	boolean verifySignatureOnly(String razorpayOrderId,
			String razorpayPaymentId, String razorpaySignature) throws Exception;
}
