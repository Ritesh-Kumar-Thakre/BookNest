package com.booknest.review.dto.request;

import lombok.Data;

@Data
public class AddReviewRequest {
	private Integer bookId;
	private Integer userId;
	private String userName;
	private Integer rating; // 1-5
	private String comment;
}
