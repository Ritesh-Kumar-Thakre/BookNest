package com.cg.wallet.dto.request;

import lombok.Data;

@Data
public class PayMoneyRequest {

	private Integer userId;

	private Integer orderId;

	private Double amount;
}