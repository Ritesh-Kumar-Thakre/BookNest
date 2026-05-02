package com.cg.wallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.wallet.entity.Statement;

public interface StatementsRepository extends JpaRepository<Statement, Integer> {

	Optional<Statement> findByStatementId(Integer statementId);

	List<Statement> findByWalletWalletId(Integer walletId);

	List<Statement> findByTransactionType(String transactionType);

	List<Statement> findByOrderId(Integer orderId);

}