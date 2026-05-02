package com.booknest.wishlist.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist_items", indexes = {
		@Index(name = "idx_wishlist_user", columnList = "userId")
}, uniqueConstraints = {
		@UniqueConstraint(columnNames = {"userId", "bookId"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer userId;

	private Integer bookId;

	private String bookTitle;

	private String coverImageUrl;

	private BigDecimal price;

	@Column(updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
