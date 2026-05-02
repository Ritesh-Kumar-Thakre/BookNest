package com.booknest.review.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.booknest.review.dto.request.AddReviewRequest;
import com.booknest.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<?> addReview(@RequestBody AddReviewRequest request) {
		try {
			return new ResponseEntity<>(reviewService.addReview(request), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to add review"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/book/{bookId}")
	public ResponseEntity<?> getByBook(@PathVariable Integer bookId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(reviewService.getReviewsByBook(bookId, pageable));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<?> getByUser(@PathVariable Integer userId) {
		return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
	}

	@GetMapping("/average/{bookId}")
	public ResponseEntity<?> getAverage(@PathVariable Integer bookId) {
		return ResponseEntity.ok(reviewService.getAverageRating(bookId));
	}

	@PutMapping("/{reviewId}")
	public ResponseEntity<?> update(@PathVariable Integer reviewId, @RequestBody AddReviewRequest request) {
		try {
			return ResponseEntity.ok(reviewService.updateReview(reviewId, request));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to update review"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<?> delete(@PathVariable Integer reviewId) {
		try {
			reviewService.deleteReview(reviewId);
			return ResponseEntity.ok(java.util.Map.of("message", "Review deleted"));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to delete review"), HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping
	public ResponseEntity<?> getAll(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(reviewService.getAllReviews(pageable));
	}

	@PutMapping("/{reviewId}/status")
	public ResponseEntity<?> moderate(@PathVariable Integer reviewId, @RequestParam String status) {
		try {
			return ResponseEntity.ok(reviewService.moderateReview(reviewId, status));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to moderate review"), HttpStatus.BAD_REQUEST);
		}
	}
}
