package com.booknest.wishlist.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booknest.wishlist.client.BookServiceClient;
import com.booknest.wishlist.dto.request.AddWishlistRequest;
import com.booknest.wishlist.dto.response.BookResponse;
import com.booknest.wishlist.dto.response.WishlistResponse;
import com.booknest.wishlist.entity.WishlistItem;
import com.booknest.wishlist.repository.WishlistRepository;
import com.booknest.wishlist.service.WishlistService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

	private static final Logger log = LoggerFactory.getLogger(WishlistServiceImpl.class);
	private final WishlistRepository wishlistRepository;
	private final BookServiceClient bookServiceClient;

	@Override
	public WishlistResponse addToWishlist(Integer userId, AddWishlistRequest request) {
		if (wishlistRepository.existsByUserIdAndBookId(userId, request.getBookId())) {
			throw new RuntimeException("Book already in wishlist");
		}

		BookResponse book = bookServiceClient.getBookById(request.getBookId());
		if (book == null) {
			throw new RuntimeException("Book not found");
		}

		WishlistItem item = WishlistItem.builder()
				.userId(userId)
				.bookId(request.getBookId())
				.bookTitle(book.getTitle())
				.coverImageUrl(book.getCoverImageUrl())
				.price(book.getPrice())
				.build();

		item = wishlistRepository.save(item);
		log.info("Added to wishlist: userId={}, bookId={}", userId, request.getBookId());
		return WishlistResponse.from(item);
	}

	@Override
	@Transactional
	public void removeFromWishlist(Integer userId, Integer bookId) {
		if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
			throw new RuntimeException("Item not found in wishlist");
		}
		wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
		log.info("Removed from wishlist: userId={}, bookId={}", userId, bookId);
	}

	@Override
	public Page<WishlistResponse> getWishlist(Integer userId, Pageable pageable) {
		return wishlistRepository.findByUserId(userId, pageable).map(WishlistResponse::from);
	}

	@Override
	public boolean isInWishlist(Integer userId, Integer bookId) {
		return wishlistRepository.existsByUserIdAndBookId(userId, bookId);
	}

	@Override
	@Transactional
	public WishlistResponse moveToCart(Integer userId, Integer itemId) {
		WishlistItem item = wishlistRepository.findById(itemId)
				.orElseThrow(() -> new RuntimeException("Wishlist item not found"));

		if (!item.getUserId().equals(userId)) {
			throw new RuntimeException("Unauthorized");
		}

		// Remove from wishlist (cart-service will be called from frontend)
		wishlistRepository.delete(item);
		log.info("Moved to cart: userId={}, bookId={}", userId, item.getBookId());
		return WishlistResponse.from(item);
	}
}
