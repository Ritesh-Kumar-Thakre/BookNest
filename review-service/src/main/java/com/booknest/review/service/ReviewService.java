package com.booknest.review.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.booknest.review.dto.request.AddReviewRequest;
import com.booknest.review.dto.response.ReviewResponse;

public interface ReviewService {
	ReviewResponse addReview(AddReviewRequest request);
	Page<ReviewResponse> getReviewsByBook(Integer bookId, Pageable pageable);
	List<ReviewResponse> getReviewsByUser(Integer userId);
	Double getAverageRating(Integer bookId);
	ReviewResponse updateReview(Integer reviewId, AddReviewRequest request);
	void deleteReview(Integer reviewId);
	Page<ReviewResponse> getAllReviews(Pageable pageable);
	ReviewResponse moderateReview(Integer reviewId, String status);
}
