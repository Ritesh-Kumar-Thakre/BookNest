package com.booknest.wishlist.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.booknest.wishlist.dto.request.AddWishlistRequest;
import com.booknest.wishlist.service.WishlistService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

	private final WishlistService wishlistService;

	@PostMapping
	public ResponseEntity<?> add(@RequestHeader("X-User-Id") Integer userId,
			@RequestBody AddWishlistRequest request) {
		try {
			return new ResponseEntity<>(wishlistService.addToWishlist(userId, request), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to add to wishlist"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/{bookId}")
	public ResponseEntity<?> remove(@RequestHeader("X-User-Id") Integer userId,
			@PathVariable Integer bookId) {
		try {
			wishlistService.removeFromWishlist(userId, bookId);
			return ResponseEntity.ok(java.util.Map.of("message", "Removed from wishlist"));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to remove from wishlist"), HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping
	public ResponseEntity<?> getAll(@RequestHeader("X-User-Id") Integer userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(wishlistService.getWishlist(userId, pageable));
	}

	@GetMapping("/check/{bookId}")
	public ResponseEntity<?> check(@RequestHeader("X-User-Id") Integer userId,
			@PathVariable Integer bookId) {
		return ResponseEntity.ok(wishlistService.isInWishlist(userId, bookId));
	}

	@PostMapping("/move-to-cart/{itemId}")
	public ResponseEntity<?> moveToCart(@RequestHeader("X-User-Id") Integer userId,
			@PathVariable Integer itemId) {
		try {
			return ResponseEntity.ok(wishlistService.moveToCart(userId, itemId));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to move to cart"), HttpStatus.BAD_REQUEST);
		}
	}
}
