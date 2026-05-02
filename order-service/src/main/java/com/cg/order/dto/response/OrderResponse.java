package com.cg.order.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

	private Integer orderId;

	private Integer userId;

	private String productName;

	private Integer quantity;

	private Double amountPaid;

	private String orderStatus;

	private LocalDate orderDate;

	private String modeOfPayment;

	// Address fields flattened
	private String fullName;
	private String mobileNumber;
	private String flatNumber;
	private String city;
	private String state;
	private String pincode;
}