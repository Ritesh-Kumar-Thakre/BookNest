package com.capgemini.cart.service;

import java.util.List;

import com.capgemini.cart.dto.request.AddToCartRequest;
import com.capgemini.cart.dto.response.CartResponse;
import com.capgemini.cart.entity.Cart;

public interface CartService {

	Cart getCartByUser(Integer userId);

	Cart addItem(Integer userId, AddToCartRequest request);

	Cart removeItem(Integer userId, Integer itemId);

	Cart updateQuantity(Integer userId, Integer itemId, Integer quantity);

	void clearCart(Integer userId);

	Double cartTotal(Cart cart);

	List<CartResponse> getAllCarts();
}
