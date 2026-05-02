package com.booknest.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.booknest.review.entity.Review;
import com.booknest.review.entity.ReviewStatus;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

	Page<Review> findByBookIdAndStatus(Integer bookId, ReviewStatus status, Pageable pageable);

	List<Review> findByUserId(Integer userId);

	Optional<Review> findByBookIdAndUserId(Integer bookId, Integer userId);

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :bookId AND r.status = 'APPROVED'")
	Double findAverageRatingByBookId(Integer bookId);

	long countByBookIdAndStatus(Integer bookId, ReviewStatus status);
}
