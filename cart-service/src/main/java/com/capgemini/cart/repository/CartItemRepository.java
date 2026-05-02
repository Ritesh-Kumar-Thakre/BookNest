package com.capgemini.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capgemini.cart.entity.Cart;
import com.capgemini.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
	Optional<CartItem> findByCartAndBookId(Cart cart, Integer bookId);

	void deleteByItemId(Integer itemId);
}