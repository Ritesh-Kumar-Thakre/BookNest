package com.capgemini.cart.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
	private Integer itemId;
	private Integer bookId;
	private String bookTitle;
	private String coverImageUrl;
	private Double price;
	private Integer quantity;
	private Double subtotal;
}
