package com.cg.order.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer orderId;

	private Integer userId;

	private LocalDate orderDate;

	private Double amountPaid;

	private String modeOfPayment;

	private String orderStatus;

	private Integer quantity;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Address address;

	@Embedded
	private Book book;

	@PrePersist
	public void prePersist() {
		this.orderDate = LocalDate.now();

		if (this.orderStatus == null) {
			this.orderStatus = "PLACED";
		}
	}
}