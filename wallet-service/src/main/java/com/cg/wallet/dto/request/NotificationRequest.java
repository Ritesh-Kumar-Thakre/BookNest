package com.cg.wallet.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {

	private Integer userId;

	private String type;

	private String message;

}