package com.booknest.review.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booknest.review.client.AuthServiceClient;
import com.booknest.review.client.BookServiceClient;
import com.booknest.review.dto.request.AddReviewRequest;
import com.booknest.review.dto.response.ReviewResponse;
import com.booknest.review.entity.Review;
import com.booknest.review.entity.ReviewStatus;
import com.booknest.review.repository.ReviewRepository;
import com.booknest.review.service.impl.ReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

	@Mock private ReviewRepository reviewRepository;
	@Mock private BookServiceClient bookServiceClient;
	@Mock private AuthServiceClient authServiceClient;
	@InjectMocks private ReviewServiceImpl reviewService;

	private Review review;
	private AddReviewRequest request;

	@BeforeEach
	void setUp() {
		review = Review.builder().reviewId(1).bookId(10).userId(100)
				.reviewerName("John").rating(4).comment("Great book")
				.status(ReviewStatus.APPROVED).verifiedPurchase(false).build();

		request = new AddReviewRequest();
		request.setBookId(10);
		request.setUserId(100);
		request.setUserName("John");
		request.setRating(4);
		request.setComment("Great book");
	}

	@Test @DisplayName("addReview creates review successfully")
	void addReview_Ok() {
		when(reviewRepository.findByBookIdAndUserId(10, 100)).thenReturn(Optional.empty());
		when(reviewRepository.save(any(Review.class))).thenReturn(review);
		when(reviewRepository.findAverageRatingByBookId(10)).thenReturn(4.0);

		ReviewResponse r = reviewService.addReview(request);
		assertNotNull(r);
		verify(reviewRepository).save(any(Review.class));
	}

	@Test @DisplayName("addReview throws on duplicate review")
	void addReview_Duplicate() {
		when(reviewRepository.findByBookIdAndUserId(10, 100)).thenReturn(Optional.of(review));
		assertThrows(RuntimeException.class, () -> reviewService.addReview(request));
	}

	@Test @DisplayName("addReview throws on invalid rating")
	void addReview_BadRating() {
		request.setRating(0);
		when(reviewRepository.findByBookIdAndUserId(10, 100)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> reviewService.addReview(request));
	}

	@Test @DisplayName("getAverageRating returns rounded average")
	void getAverageRating() {
		when(reviewRepository.findAverageRatingByBookId(10)).thenReturn(4.567);
		Double avg = reviewService.getAverageRating(10);
		assertEquals(4.6, avg);
	}

	@Test @DisplayName("getAverageRating returns 0 when null")
	void getAverageRating_Null() {
		when(reviewRepository.findAverageRatingByBookId(10)).thenReturn(null);
		assertEquals(0.0, reviewService.getAverageRating(10));
	}

	@Test @DisplayName("updateReview updates fields")
	void updateReview_Ok() {
		AddReviewRequest upd = new AddReviewRequest();
		upd.setRating(5);
		upd.setComment("Updated comment");

		when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
		when(reviewRepository.save(any(Review.class))).thenReturn(review);
		when(reviewRepository.findAverageRatingByBookId(anyInt())).thenReturn(4.5);

		ReviewResponse r = reviewService.updateReview(1, upd);
		assertNotNull(r);
		assertEquals(5, review.getRating());
	}

	@Test @DisplayName("updateReview throws when not found")
	void updateReview_NotFound() {
		when(reviewRepository.findById(99)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> reviewService.updateReview(99, request));
	}

	@Test @DisplayName("deleteReview removes review")
	void deleteReview_Ok() {
		when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
		when(reviewRepository.findAverageRatingByBookId(anyInt())).thenReturn(4.0);

		reviewService.deleteReview(1);
		verify(reviewRepository).deleteById(1);
	}

	@Test @DisplayName("deleteReview throws when not found")
	void deleteReview_NotFound() {
		when(reviewRepository.findById(99)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> reviewService.deleteReview(99));
	}

	@Test @DisplayName("moderateReview updates status")
	void moderateReview_Ok() {
		when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
		when(reviewRepository.save(any(Review.class))).thenReturn(review);
		when(reviewRepository.findAverageRatingByBookId(anyInt())).thenReturn(4.0);

		ReviewResponse r = reviewService.moderateReview(1, "REJECTED");
		assertNotNull(r);
	}
}
