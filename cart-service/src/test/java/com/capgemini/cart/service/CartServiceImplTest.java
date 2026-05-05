package com.capgemini.cart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.capgemini.cart.client.BookServiceClient;
import com.capgemini.cart.dto.request.AddToCartRequest;
import com.capgemini.cart.dto.response.BookResponse;
import com.capgemini.cart.dto.response.CartResponse;
import com.capgemini.cart.entity.Cart;
import com.capgemini.cart.entity.CartItem;
import com.capgemini.cart.repository.CartItemRepository;
import com.capgemini.cart.repository.CartRepository;
import com.capgemini.cart.service.impl.CartServiceImpl;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

	@Mock private CartRepository cartRepository;
	@Mock private CartItemRepository cartItemRepository;
	@Mock private BookServiceClient bookServiceClient;
	@InjectMocks private CartServiceImpl cartService;

	private Cart cart;
	private CartItem item;
	private BookResponse book;

	@BeforeEach
	void setUp() {
		cart = new Cart();
		cart.setCartId(1);
		cart.setUserId(100);
		cart.setTotalPrice(0.0);
		cart.setItems(new ArrayList<>());

		item = CartItem.builder().itemId(1).bookId(10).bookTitle("Book").price(30.0).quantity(2).cart(cart).build();

		book = new BookResponse();
		book.setBookId(10);
		book.setTitle("Book");
		book.setPrice(30.0);
		book.setStock(20);
	}

	@Test @DisplayName("getCartByUser returns existing cart")
	void getCart_Existing() {
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		Cart r = cartService.getCartByUser(100);
		assertNotNull(r);
		assertEquals(100, r.getUserId());
	}

	@Test @DisplayName("getCartByUser creates new cart if none")
	void getCart_New() {
		when(cartRepository.findByUserId(200)).thenReturn(null);
		Cart c = new Cart(); c.setCartId(2); c.setUserId(200); c.setTotalPrice(0.0); c.setItems(new ArrayList<>());
		when(cartRepository.save(any(Cart.class))).thenReturn(c);
		Cart r = cartService.getCartByUser(200);
		assertEquals(200, r.getUserId());
	}

	@Test @DisplayName("addItem adds new item")
	void addItem_New() {
		AddToCartRequest req = new AddToCartRequest(); req.setBookId(10); req.setQuantity(1);
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		when(bookServiceClient.getBookById(10)).thenReturn(book);
		when(cartItemRepository.findByCartAndBookId(cart, 10)).thenReturn(Optional.empty());
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		Cart r = cartService.addItem(100, req);
		assertNotNull(r);
	}

	@Test @DisplayName("addItem increments existing item qty")
	void addItem_Existing() {
		AddToCartRequest req = new AddToCartRequest(); req.setBookId(10); req.setQuantity(1);
		cart.getItems().add(item);
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		when(bookServiceClient.getBookById(10)).thenReturn(book);
		when(cartItemRepository.findByCartAndBookId(cart, 10)).thenReturn(Optional.of(item));
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		cartService.addItem(100, req);
		assertEquals(3, item.getQuantity());
	}

	@Test @DisplayName("addItem throws when book null")
	void addItem_BookNull() {
		AddToCartRequest req = new AddToCartRequest(); req.setBookId(999); req.setQuantity(1);
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		when(bookServiceClient.getBookById(999)).thenReturn(null);
		assertThrows(RuntimeException.class, () -> cartService.addItem(100, req));
	}

	@Test @DisplayName("addItem throws when out of stock")
	void addItem_OutOfStock() {
		book.setStock(0);
		AddToCartRequest req = new AddToCartRequest(); req.setBookId(10); req.setQuantity(1);
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		when(bookServiceClient.getBookById(10)).thenReturn(book);
		assertThrows(RuntimeException.class, () -> cartService.addItem(100, req));
	}

	@Test @DisplayName("removeItem removes item")
	void removeItem_Ok() {
		cart.getItems().add(item);
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		when(cartItemRepository.findById(1)).thenReturn(Optional.of(item));
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		cartService.removeItem(100, 1);
		verify(cartItemRepository).delete(item);
	}

	@Test @DisplayName("updateQuantity updates qty")
	void updateQty_Ok() {
		cart.getItems().add(item);
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		when(cartItemRepository.findById(1)).thenReturn(Optional.of(item));
		when(bookServiceClient.getBookById(10)).thenReturn(book);
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		cartService.updateQuantity(100, 1, 5);
		assertEquals(5, item.getQuantity());
	}

	@Test @DisplayName("updateQuantity throws on invalid qty")
	void updateQty_Invalid() {
		assertThrows(RuntimeException.class, () -> cartService.updateQuantity(100, 1, 0));
	}

	@Test @DisplayName("clearCart empties cart")
	void clearCart_Ok() {
		cart.getItems().add(item);
		when(cartRepository.findByUserId(100)).thenReturn(cart);
		when(cartRepository.save(any(Cart.class))).thenReturn(cart);
		cartService.clearCart(100);
		assertTrue(cart.getItems().isEmpty());
	}

	@Test @DisplayName("cartTotal calculates correctly")
	void cartTotal_Calc() {
		cart.getItems().add(item);
		assertEquals(60.0, cartService.cartTotal(cart), 0.01);
	}

	@Test @DisplayName("cartTotal returns 0 for empty")
	void cartTotal_Empty() {
		assertEquals(0.0, cartService.cartTotal(cart));
	}

	@Test @DisplayName("getAllCarts returns responses")
	void getAllCarts_Ok() {
		cart.getItems().add(item);
		when(cartRepository.findAll()).thenReturn(List.of(cart));
		List<CartResponse> r = cartService.getAllCarts();
		assertEquals(1, r.size());
	}
}
