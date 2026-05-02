package com.cg.wallet.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cg.wallet.client.NotificationServiceClient;
import com.cg.wallet.dto.request.NotificationRequest;
import com.cg.wallet.entity.Statement;
import com.cg.wallet.entity.Wallet;
import com.cg.wallet.repository.StatementsRepository;
import com.cg.wallet.repository.WalletRepository;
import com.cg.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // Lombok handles constructor injection.
public class WalletServiceImpl implements WalletService {

	private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
	private final WalletRepository walletRepository;
	private final StatementsRepository statementRepository;
	private final NotificationServiceClient notificationClient;

	@Override
	public List<Wallet> getWallets() {
		return walletRepository.findAll();
	}

	@Override
	public Wallet addWallet(Wallet wallet) {
		if (wallet.getCurrentBalance() == null) {
			wallet.setCurrentBalance(0.0);
		}
		return walletRepository.save(wallet);
	}

	@Override
	public void addMoney(Wallet wallet, Double amount, String remarks) {

		Wallet walletDb = walletRepository.findByWalletId(wallet.getWalletId())
				.orElseThrow(() -> new RuntimeException("Wallet not found"));

		walletDb.setCurrentBalance(walletDb.getCurrentBalance() + amount);
		Statement stmt = Statement.builder().transactionType("DEPOSIT").amount(amount).transactionRemarks(remarks)
				.wallet(walletDb).build();

		statementRepository.save(stmt);
		walletRepository.save(walletDb);
		log.info("Money added: walletId={}, amount={}", wallet.getWalletId(), amount);
	}

	@Override
	public void update(Wallet wallet, Double amount, String remarks, Integer orderId) {
		Wallet walletDb = walletRepository.findByWalletId(wallet.getWalletId())
				.orElseThrow(() -> new RuntimeException("Wallet not found"));
		if (walletDb.getCurrentBalance() < amount) {
			throw new RuntimeException("Insufficient balance");
		}
		walletDb.setCurrentBalance(walletDb.getCurrentBalance() - amount);
		Statement stmt = Statement.builder().transactionType("WITHDRAW").amount(amount).orderId(orderId)
				.transactionRemarks(remarks).wallet(walletDb).build();
		statementRepository.save(stmt);
		walletRepository.save(walletDb);
		log.info("Payment processed: walletId={}, amount={}, orderId={}", wallet.getWalletId(), amount, orderId);
//		notificationClient.sendNotification(

//				NotificationRequest.builder().userId(walletDb.getUserId()).type("PAYMENT_SUCCESS")
//						.message("Payment completed").build());
	}

	@Override
	public void refund(Wallet wallet, Double amount, String remarks, Integer orderId) {
		Wallet walletDb = walletRepository.findByWalletId(wallet.getWalletId())
				.orElseThrow(() -> new RuntimeException("Wallet not found"));

		walletDb.setCurrentBalance(walletDb.getCurrentBalance() + amount);

		Statement stmt = Statement.builder().transactionType("REFUND").amount(amount).orderId(orderId)
				.transactionRemarks(remarks).wallet(walletDb).build();

		statementRepository.save(stmt);
		walletRepository.save(walletDb);
		log.info("Refund processed: walletId={}, amount={}, orderId={}", wallet.getWalletId(), amount, orderId);
	}

	@Override
	public Wallet getById(Integer walletId) {
		return walletRepository.findByWalletId(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
	}

	@Override
	public List<Statement> getStatementsById(Integer walletId) {
		return statementRepository.findByWalletWalletId(walletId);
	}

	@Override
	public List<Statement> getStatements() {
		return statementRepository.findAll();
	}

	@Override
	public void deleteById(Integer walletId) {
		walletRepository.deleteByWalletId(walletId);
	}

}