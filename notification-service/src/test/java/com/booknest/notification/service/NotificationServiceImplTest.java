package com.booknest.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booknest.notification.client.AuthServiceClient;
import com.booknest.notification.dto.request.CreateNotificationRequest;
import com.booknest.notification.dto.response.NotificationResponse;
import com.booknest.notification.entity.Notification;
import com.booknest.notification.entity.NotificationType;
import com.booknest.notification.repository.NotificationRepository;
import com.booknest.notification.service.impl.NotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock private NotificationRepository notificationRepository;
	@Mock private EmailService emailService;
	@Mock private AuthServiceClient authServiceClient;
	@InjectMocks private NotificationServiceImpl notificationService;

	private Notification notification;

	@BeforeEach
	void setUp() {
		notification = Notification.builder()
				.id(1).userId(100).title("Order Placed")
				.message("Your order has been placed.").type(NotificationType.ORDER_PLACED)
				.isRead(false).build();
	}

	@Test @DisplayName("create builds and saves notification")
	void create_Ok() {
		CreateNotificationRequest req = new CreateNotificationRequest();
		req.setUserId(100);
		req.setType(NotificationType.ORDER_PLACED);
		req.setParams(Map.of("orderId", "42"));

		when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
		when(authServiceClient.getEmailByUserId(100)).thenReturn("user@test.com");

		NotificationResponse r = notificationService.create(req);
		assertNotNull(r);
		verify(notificationRepository).save(any(Notification.class));
	}

	@Test @DisplayName("create handles email failure gracefully")
	void create_EmailFails() {
		CreateNotificationRequest req = new CreateNotificationRequest();
		req.setUserId(100);
		req.setType(NotificationType.PAYMENT_SUCCESS);
		req.setParams(Map.of("amount", "500"));

		when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
		when(authServiceClient.getEmailByUserId(100)).thenThrow(new RuntimeException("Service down"));

		// Should not throw even if email fails
		NotificationResponse r = notificationService.create(req);
		assertNotNull(r);
	}

	@Test @DisplayName("getUnreadCount returns count")
	void getUnreadCount() {
		when(notificationRepository.countByUserIdAndIsReadFalse(100)).thenReturn(5L);
		assertEquals(5L, notificationService.getUnreadCount(100));
	}

	@Test @DisplayName("markAsRead sets isRead to true")
	void markAsRead_Ok() {
		when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));
		when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

		notificationService.markAsRead(1);
		assertTrue(notification.getIsRead());
	}

	@Test @DisplayName("markAsRead throws when not found")
	void markAsRead_NotFound() {
		when(notificationRepository.findById(99)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> notificationService.markAsRead(99));
	}

	@Test @DisplayName("markAllAsRead calls repository")
	void markAllAsRead_Ok() {
		notificationService.markAllAsRead(100);
		verify(notificationRepository).markAllAsRead(100);
	}

	@Test @DisplayName("delete removes notification")
	void delete_Ok() {
		when(notificationRepository.existsById(1)).thenReturn(true);
		notificationService.delete(1);
		verify(notificationRepository).deleteById(1);
	}

	@Test @DisplayName("delete throws when not found")
	void delete_NotFound() {
		when(notificationRepository.existsById(99)).thenReturn(false);
		assertThrows(RuntimeException.class, () -> notificationService.delete(99));
	}
}
