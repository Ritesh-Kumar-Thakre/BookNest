package com.cg.order.service;

import java.util.List;

import com.cg.order.dto.request.AddressRequest;
import com.cg.order.dto.request.OnlinePaymentRequest;
import com.cg.order.dto.request.PlaceOrderRequest;
import com.cg.order.dto.request.UpdateOrderStatusRequest;
import com.cg.order.dto.response.AddressResponse;
import com.cg.order.dto.response.OrderResponse;

public interface OrderService {

	List<OrderResponse> getAllOrders();

	OrderResponse getOrderById(int orderId);

	List<OrderResponse> getOrderByUserId(int userId);

	OrderResponse placeOrder(PlaceOrderRequest requestDto);

	OrderResponse onlinePayment(OnlinePaymentRequest requestDto);

	OrderResponse changeStatus(UpdateOrderStatusRequest requestDto);

	void deleteOrder(int orderId);

	AddressResponse storeAddress(AddressRequest requestDto);

	List<AddressResponse> getAddressByCustomerId(int customerId);

	List<AddressResponse> getAllAddresses();

	void deleteAddress(int addressId);

}