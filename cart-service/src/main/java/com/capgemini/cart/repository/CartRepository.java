package com.capgemini.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capgemini.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
	Cart findByCartId(Integer cartId);

	Cart findByUserId(Integer userId);

	Boolean existsByUserId(Integer userId);

	void deleteByUserId(Integer userId);
}
