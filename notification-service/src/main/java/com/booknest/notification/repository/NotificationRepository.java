package com.booknest.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.booknest.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

	Page<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

	long countByUserIdAndIsReadFalse(Integer userId);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
	void markAllAsRead(Integer userId);
}
