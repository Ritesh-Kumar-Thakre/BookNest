package com.booknest.book.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponse {
	private Integer bookId;
	private String title;
	private String author;
	private String isbn;
	private String genre;
	private String publisher;
	private Double price;
	private Integer stock;
	private Double rating;
	private String description;
	private String coverImageUrl;
	private LocalDate publishedDate;
	private boolean featured;
}