package com.cg.wallet.dto.request;

import lombok.Data;

@Data
public class AddMoneyRequest {

	private Integer userId;

	private Double amount;
}