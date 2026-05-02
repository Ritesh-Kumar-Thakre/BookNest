package com.booknest.review.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews", indexes = {
		@Index(name = "idx_review_book", columnList = "bookId"),
		@Index(name = "idx_review_user", columnList = "userId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer reviewId;

	private Integer bookId;

	private Integer userId;

	private String reviewerName;

	private Integer rating; // 1-5

	@Column(length = 1000)
	private String comment;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private ReviewStatus status = ReviewStatus.APPROVED;

	@Builder.Default
	private Boolean verifiedPurchase = false;

	@Column(updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
