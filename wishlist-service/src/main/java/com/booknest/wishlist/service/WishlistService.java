package com.booknest.wishlist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.booknest.wishlist.dto.request.AddWishlistRequest;
import com.booknest.wishlist.dto.response.WishlistResponse;

public interface WishlistService {
	WishlistResponse addToWishlist(Integer userId, AddWishlistRequest request);
	void removeFromWishlist(Integer userId, Integer bookId);
	Page<WishlistResponse> getWishlist(Integer userId, Pageable pageable);
	boolean isInWishlist(Integer userId, Integer bookId);
	WishlistResponse moveToCart(Integer userId, Integer itemId);
}
