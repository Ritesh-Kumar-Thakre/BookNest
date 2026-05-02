package com.booknest.book.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "books", indexes = {
		@Index(name = "idx_book_genre", columnList = "genre"),
		@Index(name = "idx_book_isbn", columnList = "isbn")
})
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "book_id")
	private Integer bookId;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "author", nullable = false, length = 150)
	private String author;

	@Column(name = "isbn", nullable = false, unique = true, length = 20)
	private String isbn;

	@Column(name = "genre", length = 50)
	private String genre;

	@Column(name = "publisher", length = 150)
	private String publisher;

	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "stock", nullable = false)
	private Integer stock;

	@Column(nullable = false)
	private Double rating = 0.0;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "cover_image_url")
	private String coverImageUrl;

	@Column(name = "published_date", updatable = false)
	private LocalDate publishedDate;

	@Column(nullable = false)
	private Boolean featured = false;

	@Column(name = "is_verified", nullable = false)
	private Boolean isVerified = false;

	@PrePersist
	public void prePersist() {
		this.publishedDate = LocalDate.now();
		if (this.featured == null) this.featured = false;
		if (this.rating == null) this.rating = 0.0;
		if (this.isVerified == null) this.isVerified = false;
		if (this.stock == null) this.stock = 0;
	}

}
