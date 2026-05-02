package com.cg.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {

	private Integer customerId;

	private String fullName;

	private String city;

	private String state;

}