package com.cg.wallet.dto.request;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {
	private Integer userId;
	private String type;
	private Map<String, String> params;
}