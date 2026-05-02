package com.cg.wallet.service;

import java.util.List;

import com.cg.wallet.entity.Statement;
import com.cg.wallet.entity.Wallet;

public interface WalletService {

	List<Wallet> getWallets();

	Wallet addWallet(Wallet wallet);

	void addMoney(Wallet wallet, Double amount, String remarks);

	void update(Wallet wallet, Double amount, String remarks, Integer orderId);
	
	void refund(Wallet wallet, Double amount, String remarks, Integer orderId);

	Wallet getById(Integer walletId);

	List<Statement> getStatementsById(Integer walletId);

	List<Statement> getStatements();

	void deleteById(Integer walletId);

}