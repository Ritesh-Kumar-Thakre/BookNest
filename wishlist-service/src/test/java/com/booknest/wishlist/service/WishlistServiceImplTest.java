package com.booknest.wishlist.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booknest.wishlist.client.BookServiceClient;
import com.booknest.wishlist.dto.request.AddWishlistRequest;
import com.booknest.wishlist.dto.response.BookResponse;
import com.booknest.wishlist.dto.response.WishlistResponse;
import com.booknest.wishlist.entity.WishlistItem;
import com.booknest.wishlist.repository.WishlistRepository;
import com.booknest.wishlist.service.impl.WishlistServiceImpl;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

	@Mock private WishlistRepository wishlistRepository;
	@Mock private BookServiceClient bookServiceClient;
	@InjectMocks private WishlistServiceImpl wishlistService;

	private WishlistItem item;
	private BookResponse book;

	@BeforeEach
	void setUp() {
		item = WishlistItem.builder().id(1).userId(100).bookId(10)
				.bookTitle("Test Book").price(new BigDecimal("29.99")).build();

		book = new BookResponse();
		book.setBookId(10);
		book.setTitle("Test Book");
		book.setPrice(new BigDecimal("29.99"));
	}

	@Test @DisplayName("addToWishlist adds item")
	void addToWishlist_Ok() {
		AddWishlistRequest req = new AddWishlistRequest();
		req.setBookId(10);
		when(wishlistRepository.existsByUserIdAndBookId(100, 10)).thenReturn(false);
		when(bookServiceClient.getBookById(10)).thenReturn(book);
		when(wishlistRepository.save(any(WishlistItem.class))).thenReturn(item);

		WishlistResponse r = wishlistService.addToWishlist(100, req);
		assertNotNull(r);
		verify(wishlistRepository).save(any(WishlistItem.class));
	}

	@Test @DisplayName("addToWishlist throws when already in wishlist")
	void addToWishlist_Duplicate() {
		AddWishlistRequest req = new AddWishlistRequest();
		req.setBookId(10);
		when(wishlistRepository.existsByUserIdAndBookId(100, 10)).thenReturn(true);
		assertThrows(RuntimeException.class, () -> wishlistService.addToWishlist(100, req));
	}

	@Test @DisplayName("addToWishlist throws when book not found")
	void addToWishlist_BookNotFound() {
		AddWishlistRequest req = new AddWishlistRequest();
		req.setBookId(999);
		when(wishlistRepository.existsByUserIdAndBookId(100, 999)).thenReturn(false);
		when(bookServiceClient.getBookById(999)).thenReturn(null);
		assertThrows(RuntimeException.class, () -> wishlistService.addToWishlist(100, req));
	}

	@Test @DisplayName("removeFromWishlist removes item")
	void removeFromWishlist_Ok() {
		when(wishlistRepository.existsByUserIdAndBookId(100, 10)).thenReturn(true);
		wishlistService.removeFromWishlist(100, 10);
		verify(wishlistRepository).deleteByUserIdAndBookId(100, 10);
	}

	@Test @DisplayName("removeFromWishlist throws when not found")
	void removeFromWishlist_NotFound() {
		when(wishlistRepository.existsByUserIdAndBookId(100, 999)).thenReturn(false);
		assertThrows(RuntimeException.class, () -> wishlistService.removeFromWishlist(100, 999));
	}

	@Test @DisplayName("isInWishlist returns true when exists")
	void isInWishlist_True() {
		when(wishlistRepository.existsByUserIdAndBookId(100, 10)).thenReturn(true);
		assertTrue(wishlistService.isInWishlist(100, 10));
	}

	@Test @DisplayName("isInWishlist returns false when not exists")
	void isInWishlist_False() {
		when(wishlistRepository.existsByUserIdAndBookId(100, 99)).thenReturn(false);
		assertFalse(wishlistService.isInWishlist(100, 99));
	}

	@Test @DisplayName("moveToCart removes from wishlist")
	void moveToCart_Ok() {
		when(wishlistRepository.findById(1)).thenReturn(Optional.of(item));
		WishlistResponse r = wishlistService.moveToCart(100, 1);
		assertNotNull(r);
		verify(wishlistRepository).delete(item);
	}

	@Test @DisplayName("moveToCart throws when not found")
	void moveToCart_NotFound() {
		when(wishlistRepository.findById(99)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> wishlistService.moveToCart(100, 99));
	}

	@Test @DisplayName("moveToCart throws on unauthorized access")
	void moveToCart_Unauthorized() {
		when(wishlistRepository.findById(1)).thenReturn(Optional.of(item));
		assertThrows(RuntimeException.class, () -> wishlistService.moveToCart(999, 1));
	}
}
