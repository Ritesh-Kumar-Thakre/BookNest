package com.cg.wallet.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatementResponse {

	private Integer statementId;

	private String transactionType;

	private Double amount;

	private LocalDateTime dateTime;

	private Integer orderId;

	private String transactionRemarks;
}