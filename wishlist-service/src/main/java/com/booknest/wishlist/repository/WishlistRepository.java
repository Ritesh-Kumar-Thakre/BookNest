package com.booknest.wishlist.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.booknest.wishlist.entity.WishlistItem;

public interface WishlistRepository extends JpaRepository<WishlistItem, Integer> {

	Page<WishlistItem> findByUserId(Integer userId, Pageable pageable);

	Optional<WishlistItem> findByUserIdAndBookId(Integer userId, Integer bookId);

	boolean existsByUserIdAndBookId(Integer userId, Integer bookId);

	void deleteByUserIdAndBookId(Integer userId, Integer bookId);
}
