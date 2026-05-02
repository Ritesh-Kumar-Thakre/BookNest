package com.capgemini.cart.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponse {
	private Integer cartId;
	private Integer userId;
	private List<CartItemResponse> items;
	private Double totalPrice;
	private Integer totalItems;
}