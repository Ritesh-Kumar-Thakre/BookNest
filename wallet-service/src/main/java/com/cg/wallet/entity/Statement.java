package com.cg.wallet.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "statements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer statementId;

	private String transactionType;

	private Double amount;

	private LocalDateTime dateTime;

	private Integer orderId;

	private String transactionRemarks;

	@ManyToOne
	@JoinColumn(name = "wallet_id")
	@JsonIgnore
	private Wallet wallet;

	@PrePersist
	public void prePersist() {
		this.dateTime = LocalDateTime.now();
	}

}