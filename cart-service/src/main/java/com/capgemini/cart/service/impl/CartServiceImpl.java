package com.capgemini.cart.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.capgemini.cart.client.BookServiceClient;
import com.capgemini.cart.dto.request.AddToCartRequest;
import com.capgemini.cart.dto.response.BookResponse;
import com.capgemini.cart.dto.response.CartResponse;
import com.capgemini.cart.entity.Cart;
import com.capgemini.cart.entity.CartItem;
import com.capgemini.cart.repository.CartItemRepository;
import com.capgemini.cart.repository.CartRepository;
import com.capgemini.cart.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final BookServiceClient bookServiceClient;

	@Override
	public Cart getCartByUser(Integer userId) {
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null) {
			cart = new Cart();
			cart.setUserId(userId);
			cart.setTotalPrice(0.0);
			cart = cartRepository.save(cart);
		}
		cart.setTotalPrice(cartTotal(cart));
		return cart;
	}

	@Override
	public Cart addItem(Integer userId, AddToCartRequest request) {
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null) {
			cart = new Cart();
			cart.setUserId(userId);
			cart.setTotalPrice(0.0);

			cart = cartRepository.save(cart);
		}

		BookResponse book = bookServiceClient.getBookById(request.getBookId());
		if (book == null) {
			throw new RuntimeException("Book not found");
		}

		// Check if book is out of stock
		if (book.getStock() <= 0) {
			throw new RuntimeException("This book is out of stock");
		}

		Optional<CartItem> existingItem = cartItemRepository.findByCartAndBookId(cart, request.getBookId());
		if (existingItem.isPresent()) {
			CartItem item = existingItem.get();
			int newQty = item.getQuantity() + request.getQuantity();

			// Validate total quantity doesn't exceed available stock
			if (newQty > book.getStock()) {
				throw new RuntimeException("Cannot add more. Only " + book.getStock() + " available in stock. You already have " + item.getQuantity() + " in cart.");
			}

			item.setQuantity(newQty);
			cartItemRepository.save(item);
		} else {
			if (request.getQuantity() > book.getStock()) {
				throw new RuntimeException("Insufficient stock. Only " + book.getStock() + " available.");
			}
			CartItem item = CartItem.builder().bookId(book.getBookId()).bookTitle(book.getTitle())
					.coverImageUrl(book.getCoverImageUrl())
					.price(book.getPrice()).quantity(request.getQuantity()).cart(cart).build();
			cart.getItems().add(item);
		}
		cart.setTotalPrice(cartTotal(cart));
		log.info("Item added to cart: userId={}, bookId={}, qty={}", userId, request.getBookId(), request.getQuantity());
		return cartRepository.save(cart);
	}

	@Override
	public Cart removeItem(Integer userId, Integer itemId) {
		Cart cart = getCartByUser(userId);
		CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
		cart.getItems().remove(item);
		cartItemRepository.delete(item);
		cart.setTotalPrice(cartTotal(cart));

		return cartRepository.save(cart);
	}

	@Override
	public Cart updateQuantity(Integer userId, Integer itemId, Integer quantity) {
		if (quantity < 1) {
			throw new RuntimeException("Invalid quantity");
		}
		Cart cart = getCartByUser(userId);
		CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
		BookResponse book = bookServiceClient.getBookById(item.getBookId());
		if (quantity > book.getStock()) {
			throw new RuntimeException("Cannot set quantity to " + quantity + ". Only " + book.getStock() + " available in stock.");
		}
		item.setQuantity(quantity);
		cartItemRepository.save(item);
		cart.setTotalPrice(cartTotal(cart));
		return cartRepository.save(cart);
	}

	@Override
	public void clearCart(Integer userId) {
		Cart cart = getCartByUser(userId);
		cart.getItems().clear();
		cart.setTotalPrice(0.0);
		cartRepository.save(cart);
	}

	@Override
	public Double cartTotal(Cart cart) {
		Double total = 0.0;
		for (CartItem item : cart.getItems()) {
			total += item.getPrice() * item.getQuantity();
		}

		return total;
	}

	@Override
	public List<CartResponse> getAllCarts() {

		return cartRepository.findAll().stream().map(cart -> CartResponse.builder().cartId(cart.getCartId())
				.userId(cart.getUserId()).totalPrice(cart.getTotalPrice()).totalItems(cart.getItems().size()).build())
				.toList();
	}

}
