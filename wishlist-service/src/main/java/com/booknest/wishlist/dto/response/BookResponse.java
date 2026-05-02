package com.booknest.wishlist.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BookResponse {
	private Integer bookId;
	private String title;
	private String author;
	private BigDecimal price;
	private Integer stock;
	private String coverImageUrl;
}
