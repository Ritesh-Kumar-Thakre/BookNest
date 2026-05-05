package com.cg.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cg.order.client.BookServiceClient;
import com.cg.order.client.WalletServiceClient;
import com.cg.order.config.NotificationPublisher;
import com.cg.order.dto.request.AddressRequest;
import com.cg.order.dto.request.OnlinePaymentRequest;
import com.cg.order.dto.request.PlaceOrderRequest;
import com.cg.order.dto.request.UpdateOrderStatusRequest;
import com.cg.order.dto.response.AddressResponse;
import com.cg.order.dto.response.OrderResponse;
import com.cg.order.entity.Address;
import com.cg.order.entity.Book;
import com.cg.order.entity.Order;
import com.cg.order.repository.AddressRepository;
import com.cg.order.repository.OrderRepository;
import com.cg.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

	private final OrderRepository orderRepository;
	private final AddressRepository addressRepository;
	private final BookServiceClient bookClient;
	private final WalletServiceClient walletClient;
	private final NotificationPublisher notificationPublisher;

	@Override
	public List<OrderResponse> getAllOrders() {
		log.info("Fetching all orders");
		return orderRepository.findAll().stream().map(this::map).toList();
	}

	@Override
	public OrderResponse getOrderById(int orderId) {
		log.info("Fetching order by id={}", orderId);
		return map(orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found")));
	}

	@Override
	public List<OrderResponse> getOrderByUserId(int userId) {
		log.info("Fetching orders for userId={}", userId);
		return orderRepository.findByUserIdOrderByOrderIdDesc(userId).stream().map(this::map).toList();
	}

	@Override
	public OrderResponse placeOrder(PlaceOrderRequest req) {
		log.info("Placing order for userId={}, productId={}", req.getUserId(), req.getProductId());

		// ---- Address Validation ----
		validateNotBlank(req.getFlatNumber(), "Street/Flat Number");
		validateNotBlank(req.getCity(), "City");
		validateNotBlank(req.getState(), "State");
		validateNotBlank(req.getPincode(), "PIN Code");

		var bookData = bookClient.getBookById(req.getProductId());

		if (bookData.getStock() < req.getQuantity()) {
			log.warn("Insufficient stock for bookId={}: available={}, requested={}", req.getProductId(), bookData.getStock(), req.getQuantity());
			throw new RuntimeException("Insufficient stock. Only " + bookData.getStock() + " available.");
		}

		Address address = new Address();
		address.setFullName(req.getFullName());
		address.setMobileNumber(req.getMobileNumber());
		address.setFlatNumber(req.getFlatNumber());
		address.setCity(req.getCity());
		address.setPincode(req.getPincode());
		address.setState(req.getState());

		address = addressRepository.save(address);

		Book book = new Book(bookData.getBookId(), bookData.getTitle());

		Order order = new Order();
		order.setUserId(req.getUserId());
		order.setQuantity(req.getQuantity());
		order.setModeOfPayment(req.getModeOfPayment());
		order.setAddress(address);
		order.setBook(book);

		order.setAmountPaid(bookData.getPrice() * req.getQuantity());

		order = orderRepository.save(order);
		log.info("Order placed successfully: orderId={}, userId={}", order.getOrderId(), req.getUserId());

		// ---- Reduce stock in book-service ----
		try {
			int newStock = bookData.getStock() - req.getQuantity();
			bookClient.updateStock(Map.of(
				"bookId", String.valueOf(bookData.getBookId()),
				"stock", String.valueOf(Math.max(newStock, 0))
			));
			log.info("Stock reduced for bookId={}: newStock={}", bookData.getBookId(), Math.max(newStock, 0));
		} catch (Exception e) {
			log.error("Failed to reduce stock for bookId={}: {}", bookData.getBookId(), e.getMessage());
		}

		// ---- Send notification via RabbitMQ ----
		notificationPublisher.sendNotification(order.getUserId(), "ORDER_PLACED", order.getOrderId(), order.getAmountPaid(), false);

		return map(order);
	}

	@Override
	public OrderResponse onlinePayment(OnlinePaymentRequest req) {
		log.info("Processing online payment for orderId={}", req.getOrderId());

		Order order = orderRepository.findById(req.getOrderId())
				.orElseThrow(() -> new RuntimeException("Order not found"));

		var balanceResponse = walletClient.getBalance(order.getUserId());
		Double balance = balanceResponse.get("balance");

		if (balance == null || balance < order.getAmountPaid()) {
			log.warn("Insufficient balance for userId={}: balance={}, required={}", order.getUserId(), balance, order.getAmountPaid());
			throw new RuntimeException("Insufficient balance");
		}

		var payResponse = walletClient.payMoney(order.getUserId(), order.getAmountPaid(), order.getOrderId());
		if (payResponse == null || !Boolean.TRUE.equals(payResponse.get("success"))) {
			log.error("Payment failed for orderId={}", req.getOrderId());
			throw new RuntimeException("Payment failed");
		}

		order.setOrderStatus("CONFIRMED");
		order = orderRepository.save(order);
		log.info("Payment successful: orderId={}, amount={}", order.getOrderId(), order.getAmountPaid());

		// ---- Send notification via RabbitMQ ----
		notificationPublisher.sendNotification(order.getUserId(), "PAYMENT_SUCCESS", order.getOrderId(), order.getAmountPaid(), false);

		return map(order);
	}

	@Override
	public OrderResponse changeStatus(UpdateOrderStatusRequest req) {
		log.info("Request to change order status: orderId={}, targetStatus={}", req.getOrderId(), req.getOrderStatus());

		Order order = orderRepository.findById(req.getOrderId())
				.orElseThrow(() -> new RuntimeException("Order not found with ID: " + req.getOrderId()));

		// 1. Prevent changing status if already in a terminal state (DELIVERED or CANCELLED)
		String currentStatus = order.getOrderStatus() != null ? order.getOrderStatus().toUpperCase() : "";
		if ("DELIVERED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
			log.warn("Attempted to change status of terminal order: orderId={}, currentStatus={}", order.getOrderId(), currentStatus);
			throw new RuntimeException("Order is already " + currentStatus + " and cannot be modified.");
		}

		String newStatus = req.getOrderStatus().toUpperCase();
		Double amount = order.getAmountPaid() != null ? order.getAmountPaid() : 0.0;
		
		// 2. Handle Cancellation logic (Refund & Stock Restoration)
		if ("CANCELLED".equals(newStatus)) {
			// Refund Wallet if it was a wallet payment
			if ("WALLET".equalsIgnoreCase(order.getModeOfPayment()) || "RAZORPAY".equalsIgnoreCase(order.getModeOfPayment())) {
				try {
					log.info("Refunding wallet: userId={}, amount={}, orderId={}", order.getUserId(), amount, order.getOrderId());
					walletClient.refundMoney(order.getUserId(), amount, order.getOrderId());
				} catch (Exception e) {
					log.error("Wallet refund failed for order {}: {}", order.getOrderId(), e.getMessage());
					// We might want to continue or throw depending on business rules, 
					// but for now let's throw to ensure data consistency
					throw new RuntimeException("Refund failed: " + e.getMessage());
				}
			}
			
			// Restore Stock
			if (order.getBook() != null && order.getBook().getProductId() != null) {
				try {
					Integer bookId = order.getBook().getProductId();
					Integer quantityToRestore = order.getQuantity();
					
					log.info("Restoring stock for bookId={}: current qty in order={}", bookId, quantityToRestore);
					
					// 1. Fetch current book details to get latest stock
					var bookResponse = bookClient.getBookById(bookId);
					if (bookResponse != null && bookResponse.getStock() != null) {
						int currentStock = bookResponse.getStock();
						int newStock = currentStock + quantityToRestore;
						
						log.info("Calculating new stock: {} + {} = {}", currentStock, quantityToRestore, newStock);
						
						// 2. Update stock using absolute value (most compatible)
						Map<String, String> stockReq = new HashMap<>();
						stockReq.put("bookId", String.valueOf(bookId));
						stockReq.put("stock", String.valueOf(newStock));
						// Also keep 'change' for newer book-service versions
						stockReq.put("change", String.valueOf(quantityToRestore)); 
						
						bookClient.updateStock(stockReq);
						log.info("Stock restoration request sent successfully for bookId={}", bookId);
					} else {
						log.warn("Could not fetch book details for stock restoration: bookId={}", bookId);
					}
				} catch (Exception e) {
					log.error("Stock restoration failed for order {}: {}", order.getOrderId(), e.getMessage());
				}
			}
		}

		order.setOrderStatus(newStatus);
		order = orderRepository.save(order);
		log.info("Order status successfully updated: orderId={}, finalStatus={}", order.getOrderId(), order.getOrderStatus());

		// 3. Send notification
		try {
			String notifType = mapStatusToNotificationType(newStatus);
			Boolean isRefunded = "CANCELLED".equals(newStatus) ? ("WALLET".equalsIgnoreCase(order.getModeOfPayment()) || "RAZORPAY".equalsIgnoreCase(order.getModeOfPayment())) : null;
			notificationPublisher.sendNotification(order.getUserId(), notifType, order.getOrderId(), amount, isRefunded);
		} catch (Exception e) {
			log.warn("Notification failed for order {}: {}", order.getOrderId(), e.getMessage());
		}

		return map(order);
	}

	@Override
	public void deleteOrder(int orderId) {
		log.info("Deleting order: orderId={}", orderId);
		orderRepository.deleteById(orderId);
	}

	@Override
	public AddressResponse storeAddress(AddressRequest req) {
		log.info("Storing address for fullName={}", req.getFullName());
		Address a = new Address();
		a.setFullName(req.getFullName());
		a.setMobileNumber(req.getMobileNumber());
		a.setFlatNumber(req.getFlatNumber());
		a.setCity(req.getCity());
		a.setPincode(req.getPincode());
		a.setState(req.getState());

		a = addressRepository.save(a);

		return AddressResponse.builder().customerId(a.getCustomerId()).fullName(a.getFullName()).city(a.getCity())
				.state(a.getState()).build();
	}

	@Override
	public List<AddressResponse> getAddressByCustomerId(int customerId) {
		log.debug("Fetching addresses for customerId={}", customerId);
		return addressRepository.findByCustomerId(customerId).stream().map(a -> AddressResponse.builder()
				.customerId(a.getCustomerId()).fullName(a.getFullName()).city(a.getCity()).state(a.getState()).build())
				.toList();
	}

	@Override
	public List<AddressResponse> getAllAddresses() {
		log.debug("Fetching all addresses");
		return addressRepository.findAll().stream().map(a -> AddressResponse.builder().customerId(a.getCustomerId())
				.fullName(a.getFullName()).city(a.getCity()).state(a.getState()).build()).toList();
	}

	@Override
	public void deleteAddress(int addressId) {
		log.info("Deleting address: addressId={}", addressId);
		addressRepository.deleteById(addressId);
	}

	private OrderResponse map(Order o) {
		var builder = OrderResponse.builder()
				.orderId(o.getOrderId())
				.userId(o.getUserId())
				.productName(o.getBook() != null ? o.getBook().getProductName() : null)
				.quantity(o.getQuantity())
				.amountPaid(o.getAmountPaid())
				.orderStatus(o.getOrderStatus())
				.orderDate(o.getOrderDate())
				.modeOfPayment(o.getModeOfPayment());

		if (o.getAddress() != null) {
			builder.fullName(o.getAddress().getFullName())
					.mobileNumber(o.getAddress().getMobileNumber())
					.flatNumber(o.getAddress().getFlatNumber())
					.city(o.getAddress().getCity())
					.state(o.getAddress().getState())
					.pincode(o.getAddress().getPincode());
		}

		return builder.build();
	}

	/**
	 * Validates that a field is not null, not empty, not "null" / "undefined" strings.
	 */
	private void validateNotBlank(String value, String fieldName) {
		if (value == null || value.trim().isEmpty()
				|| "null".equalsIgnoreCase(value.trim())
				|| "undefined".equalsIgnoreCase(value.trim())) {
			throw new RuntimeException(fieldName + " is required. Please provide a valid address.");
		}
	}

	/**
	 * Maps order status string to NotificationType enum name.
	 */
	private String mapStatusToNotificationType(String orderStatus) {
		if (orderStatus == null) return "SYSTEM";
		return switch (orderStatus.toUpperCase()) {
			case "PLACED" -> "ORDER_PLACED";
			case "CONFIRMED" -> "ORDER_CONFIRMED";
			case "SHIPPED", "DISPATCHED" -> "ORDER_DISPATCHED";
			case "DELIVERED" -> "ORDER_DELIVERED";
			case "CANCELLED" -> "ORDER_CANCELLED";
			default -> "SYSTEM";
		};
	}
}