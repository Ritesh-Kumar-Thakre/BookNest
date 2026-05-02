package com.booknest.wishlist.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.booknest.wishlist.entity.WishlistItem;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistResponse {
	private Integer id;
	private Integer bookId;
	private String bookTitle;
	private String coverImageUrl;
	private BigDecimal price;
	private LocalDateTime createdAt;

	public static WishlistResponse from(WishlistItem item) {
		return WishlistResponse.builder()
				.id(item.getId())
				.bookId(item.getBookId())
				.bookTitle(item.getBookTitle())
				.coverImageUrl(item.getCoverImageUrl())
				.price(item.getPrice())
				.createdAt(item.getCreatedAt())
				.build();
	}
}
