package com.capgemini.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestHeader;
import com.capgemini.cart.dto.request.AddToCartRequest;
import com.capgemini.cart.entity.Cart;
import com.capgemini.cart.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {
	@Autowired
	CartService cartService;

	@GetMapping
	public ResponseEntity<?> getCart(@RequestHeader("X-User-Id") Integer userId) {
		return new ResponseEntity<>(cartService.getCartByUser(userId), HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<?> addItem(@RequestHeader("X-User-Id") Integer userId, @RequestBody AddToCartRequest request) {
		try {
			return new ResponseEntity<>(cartService.addItem(userId, request), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to add item"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/remove/{itemId}")
	public ResponseEntity<?> removeItem(@RequestHeader("X-User-Id") Integer userId, @PathVariable Integer itemId) {
		return new ResponseEntity<>(cartService.removeItem(userId, itemId), HttpStatus.OK);
	}

	@PatchMapping("/update/{itemId}/{quantity}")
	public ResponseEntity<?> updateQuantity(@RequestHeader("X-User-Id") Integer userId, @PathVariable Integer itemId,
			@PathVariable Integer quantity) {
		try {
			return new ResponseEntity<>(cartService.updateQuantity(userId, itemId, quantity), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to update quantity"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/clear")
	public ResponseEntity<?> clearCart(@RequestHeader("X-User-Id") Integer userId) {
		cartService.clearCart(userId);
		return new ResponseEntity<>(java.util.Map.of("message", "Cart cleared"), HttpStatus.OK);
	}

	@GetMapping("/getAll")
	public ResponseEntity<?> getAllCarts() {
		try {
			return new ResponseEntity<>(cartService.getAllCarts(), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to get carts"), HttpStatus.BAD_REQUEST);
		}
	}
}
