package com.cg.wallet.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletResponse {

	private Integer walletId;

	private Integer userId;

	private Double currentBalance;
}