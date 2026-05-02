package com.booknest.notification.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booknest.notification.client.AuthServiceClient;
import com.booknest.notification.dto.request.CreateNotificationRequest;
import com.booknest.notification.dto.response.NotificationResponse;
import com.booknest.notification.entity.Notification;
import com.booknest.notification.entity.NotificationType;
import com.booknest.notification.repository.NotificationRepository;
import com.booknest.notification.service.EmailService;
import com.booknest.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
	private final NotificationRepository notificationRepository;
	private final EmailService emailService;
	private final AuthServiceClient authServiceClient;

	@Override
	public NotificationResponse create(CreateNotificationRequest request) {
		String title = buildTitle(request.getType());
		String message = buildMessage(request.getType(), request.getParams());

		Notification notification = Notification.builder()
				.userId(request.getUserId())
				.title(title)
				.message(message)
				.type(request.getType())
				.isRead(false)
				.build();

		notification = notificationRepository.save(notification);
		log.info("Notification created: userId={}, type={}", request.getUserId(), request.getType());

		// Try to send email (graceful failure)
		try {
			String email = authServiceClient.getEmailByUserId(request.getUserId());
			emailService.sendEmail(email, title, message);
		} catch (Exception e) {
			log.warn("Could not send email for notification: {}", e.getMessage());
		}

		return NotificationResponse.from(notification);
	}

	@Override
	public Page<NotificationResponse> getByUser(Integer userId, Pageable pageable) {
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
				.map(NotificationResponse::from);
	}

	@Override
	public long getUnreadCount(Integer userId) {
		return notificationRepository.countByUserIdAndIsReadFalse(userId);
	}

	@Override
	public void markAsRead(Integer notificationId) {
		Notification n = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new RuntimeException("Notification not found"));
		n.setIsRead(true);
		notificationRepository.save(n);
	}

	@Override
	@Transactional
	public void markAllAsRead(Integer userId) {
		notificationRepository.markAllAsRead(userId);
	}

	@Override
	public void delete(Integer notificationId) {
		if (!notificationRepository.existsById(notificationId)) {
			throw new RuntimeException("Notification not found");
		}
		notificationRepository.deleteById(notificationId);
	}

	private String buildTitle(NotificationType type) {
		return switch (type) {
			case ORDER_PLACED -> "Order Placed Successfully";
			case ORDER_CONFIRMED -> "Order Confirmed";
			case ORDER_DISPATCHED -> "Your Order Has Been Dispatched";
			case ORDER_DELIVERED -> "Order Delivered";
			case ORDER_CANCELLED -> "Order Cancelled";
			case PAYMENT_SUCCESS -> "Payment Successful";
			case WALLET_CREDIT -> "Wallet Credited";
			case SYSTEM -> "BookNest Update";
		};
	}

	private String buildMessage(NotificationType type, Map<String, String> params) {
		if (params == null) params = Map.of();
		String orderId = params.getOrDefault("orderId", "");
		String amount = params.getOrDefault("amount", "");

		return switch (type) {
			case ORDER_PLACED -> "Your order #" + orderId + " has been placed successfully.";
			case ORDER_CONFIRMED -> "Your order #" + orderId + " has been confirmed.";
			case ORDER_DISPATCHED -> "Your order #" + orderId + " has been dispatched and is on its way.";
			case ORDER_DELIVERED -> "Your order #" + orderId + " has been delivered. Enjoy your books!";
			case ORDER_CANCELLED -> {
				String base = "Your order #" + orderId + " has been cancelled.";
				String isRefunded = params.getOrDefault("isRefunded", "false");
				if (!amount.isEmpty()) {
					if ("true".equals(isRefunded)) {
						yield base + " ₹" + amount + " refunded to your wallet.";
					} else {
						yield base + " ₹" + amount + " will be credited to your wallet in 3 business days.";
					}
				}
				yield base;
			}
			case PAYMENT_SUCCESS -> "Payment of ₹" + amount + " was successful.";
			case WALLET_CREDIT -> "₹" + amount + " has been added to your wallet.";
			case SYSTEM -> params.getOrDefault("message", "You have a new notification from BookNest.");
		};
	}
}
