package com.booknest.book.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "book_id", nullable = false)
	private Integer bookId;

	@Column(name = "image_url", nullable = false)
	private String imageUrl;

	@Column(name = "is_primary")
	private Boolean isPrimary;

	@Column(name = "display_order")
	private Integer displayOrder;
}
