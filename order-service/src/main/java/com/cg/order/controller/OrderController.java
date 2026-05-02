package com.cg.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cg.order.dto.request.OnlinePaymentRequest;
import com.cg.order.dto.request.PlaceOrderRequest;
import com.cg.order.dto.request.UpdateOrderStatusRequest;
import com.cg.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@GetMapping
	public ResponseEntity<?> getAll() {
		return ResponseEntity.ok(orderService.getAllOrders());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getById(@PathVariable int id) {
		return ResponseEntity.ok(orderService.getOrderById(id));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<?> getByUser(@PathVariable int userId) {
		return ResponseEntity.ok(orderService.getOrderByUserId(userId));
	}

	@PostMapping("/place")
	public ResponseEntity<?> place(@RequestBody PlaceOrderRequest req) {
		try {
			return new ResponseEntity<>(orderService.placeOrder(req), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to place order"), HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/online")
	public ResponseEntity<?> online(@RequestBody OnlinePaymentRequest req) {
		try {
			return ResponseEntity.ok(orderService.onlinePayment(req));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Payment failed"), HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/status")
	public ResponseEntity<?> status(@RequestBody UpdateOrderStatusRequest req) {
		try {
			return ResponseEntity.ok(orderService.changeStatus(req));
		} catch (Exception e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Failed to update status"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable int id) {
		orderService.deleteOrder(id);
		return ResponseEntity.ok("Deleted");
	}
}