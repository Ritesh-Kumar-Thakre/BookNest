package com.cg.order.dto.request;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

	private Integer orderId;

	private String orderStatus;

}