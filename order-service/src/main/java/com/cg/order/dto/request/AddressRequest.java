package com.cg.order.dto.request;

import lombok.Data;

@Data
public class AddressRequest {

	private String fullName;

	private String mobileNumber;

	private String flatNumber;

	private String city;

	private String pincode;

	private String state;

}
