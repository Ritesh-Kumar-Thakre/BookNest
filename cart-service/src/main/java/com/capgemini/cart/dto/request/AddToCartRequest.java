package com.capgemini.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
	@NotNull
	private Integer bookId;
	@Min(1)
	private Integer quantity;
}