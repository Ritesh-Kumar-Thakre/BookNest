package com.booknest.book.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddBookRequest {
	@NotBlank
	private String title;
	@NotBlank
	private String author;
	private String isbn;
	private String genre;
	private String publisher;
	@Min(0)
	private Double price;
	@Min(0)
	private Integer stock;
	private String description;
	private String coverImageUrl;
	private LocalDate publishedDate;
	private boolean featured;
}