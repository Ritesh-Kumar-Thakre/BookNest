package com.cg.order.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer customerId;

	private String fullName;

	private String mobileNumber;

	private String flatNumber;

	private String city;

	private String pincode;

	private String state;
}