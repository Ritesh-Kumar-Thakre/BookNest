package com.booknest.review.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booknest.review.dto.request.AddReviewRequest;
import com.booknest.review.dto.response.ReviewResponse;
import com.booknest.review.entity.Review;
import com.booknest.review.entity.ReviewStatus;
import com.booknest.review.client.AuthServiceClient;
import com.booknest.review.client.BookServiceClient;
import com.booknest.review.repository.ReviewRepository;
import com.booknest.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);
	private final ReviewRepository reviewRepository;
	private final BookServiceClient bookServiceClient;
	private final AuthServiceClient authServiceClient;

	@Override
	public ReviewResponse addReview(AddReviewRequest request) {
		log.info("Adding review: bookId={}, userId={}", request.getBookId(), request.getUserId());

		// Check if user already reviewed this book
		reviewRepository.findByBookIdAndUserId(request.getBookId(), request.getUserId())
				.ifPresent(r -> { throw new RuntimeException("You have already reviewed this book"); });

		if (request.getRating() < 1 || request.getRating() > 5) {
			throw new RuntimeException("Rating must be between 1 and 5");
		}

		// Resolve reviewer name: use provided name, or fetch from auth-service, or default
		String reviewerName = request.getUserName();
		if (reviewerName == null || reviewerName.isBlank()) {
			try {
				reviewerName = authServiceClient.getNameByUserId(request.getUserId());
				if (reviewerName == null || reviewerName.isBlank()) {
					reviewerName = "User";
				}
			} catch (Exception e) {
				log.warn("Failed to fetch reviewer name for userId={}: {}", request.getUserId(), e.getMessage());
				reviewerName = "User";
			}
		}

		Review review = Review.builder()
				.bookId(request.getBookId())
				.userId(request.getUserId())
				.reviewerName(reviewerName)
				.rating(request.getRating())
				.comment(request.getComment())
				.status(ReviewStatus.APPROVED)
				.verifiedPurchase(false)
				.build();

		review = reviewRepository.save(review);
		log.info("Review added successfully: reviewId={}, bookId={}, userId={}", review.getReviewId(), request.getBookId(), request.getUserId());

		// Update book rating via Feign
		try {
			Double avgRating = getAverageRating(request.getBookId());
			bookServiceClient.updateBookRating(request.getBookId(), avgRating);
			log.info("Book rating updated: bookId={}, avgRating={}", request.getBookId(), avgRating);
		} catch (Exception e) {
			log.warn("Failed to update book rating for bookId={}: {}", request.getBookId(), e.getMessage());
		}

		return ReviewResponse.from(review);
	}

	@Override
	public Page<ReviewResponse> getReviewsByBook(Integer bookId, Pageable pageable) {
		log.debug("Fetching reviews for bookId={}", bookId);
		return reviewRepository.findByBookIdAndStatus(bookId, ReviewStatus.APPROVED, pageable)
				.map(ReviewResponse::from);
	}

	@Override
	public List<ReviewResponse> getReviewsByUser(Integer userId) {
		log.debug("Fetching reviews for userId={}", userId);
		return reviewRepository.findByUserId(userId).stream()
				.map(ReviewResponse::from)
				.toList();
	}

	@Override
	public Double getAverageRating(Integer bookId) {
		Double avg = reviewRepository.findAverageRatingByBookId(bookId);
		return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
	}

	@Override
	public ReviewResponse updateReview(Integer reviewId, AddReviewRequest request) {
		log.info("Updating review: reviewId={}", reviewId);
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

		if (request.getRating() != null) review.setRating(request.getRating());
		if (request.getComment() != null) review.setComment(request.getComment());

		Review updatedReview = reviewRepository.save(review);
		updateBookRating(updatedReview.getBookId());
		return ReviewResponse.from(updatedReview);
	}

	@Override
	public void deleteReview(Integer reviewId) {
		log.info("Deleting review: reviewId={}", reviewId);
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
		Integer bookId = review.getBookId();
		
		reviewRepository.deleteById(reviewId);
		log.info("Review deleted successfully: reviewId={}", reviewId);
		
		updateBookRating(bookId);
	}

	@Override
	public Page<ReviewResponse> getAllReviews(Pageable pageable) {
		log.debug("Fetching all reviews");
		return reviewRepository.findAll(pageable).map(ReviewResponse::from);
	}

	@Override
	public ReviewResponse moderateReview(Integer reviewId, String status) {
		log.info("Moderating review: reviewId={}, newStatus={}", reviewId, status);
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
		review.setStatus(ReviewStatus.valueOf(status.toUpperCase()));
		Review updatedReview = reviewRepository.save(review);
		
		updateBookRating(updatedReview.getBookId());
		return ReviewResponse.from(updatedReview);
	}

	private void updateBookRating(Integer bookId) {
		try {
			Double avgRating = getAverageRating(bookId);
			bookServiceClient.updateBookRating(bookId, avgRating);
			log.info("Book rating updated: bookId={}, avgRating={}", bookId, avgRating);
		} catch (Exception e) {
			log.warn("Failed to update book rating for bookId={}: {}", bookId, e.getMessage());
		}
	}
}
