package com.booknest.review.dto.response;

import java.time.LocalDateTime;

import com.booknest.review.entity.Review;
import com.booknest.review.entity.ReviewStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
	private Integer reviewId;
	private Integer bookId;
	private Integer userId;
	private String reviewerName;
	private Integer rating;
	private String comment;
	private ReviewStatus status;
	private Boolean verifiedPurchase;
	private LocalDateTime createdAt;

	public static ReviewResponse from(Review r) {
		return ReviewResponse.builder()
				.reviewId(r.getReviewId())
				.bookId(r.getBookId())
				.userId(r.getUserId())
				.reviewerName(r.getReviewerName())
				.rating(r.getRating())
				.comment(r.getComment())
				.status(r.getStatus())
				.verifiedPurchase(r.getVerifiedPurchase())
				.createdAt(r.getCreatedAt())
				.build();
	}
}
