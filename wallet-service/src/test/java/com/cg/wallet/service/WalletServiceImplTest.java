package com.cg.wallet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cg.wallet.client.NotificationServiceClient;
import com.cg.wallet.entity.Statement;
import com.cg.wallet.entity.Wallet;
import com.cg.wallet.repository.StatementsRepository;
import com.cg.wallet.repository.WalletRepository;
import com.cg.wallet.service.impl.WalletServiceImpl;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

	@Mock private WalletRepository walletRepository;
	@Mock private StatementsRepository statementRepository;
	@Mock private NotificationServiceClient notificationClient;
	@InjectMocks private WalletServiceImpl walletService;

	private Wallet wallet;

	@BeforeEach
	void setUp() {
		wallet = new Wallet();
		wallet.setWalletId(1);
		wallet.setUserId(100);
		wallet.setCurrentBalance(500.0);
		wallet.setStatements(new ArrayList<>());
	}

	@Test @DisplayName("getWallets returns all wallets")
	void getWallets() {
		when(walletRepository.findAll()).thenReturn(List.of(wallet));
		assertEquals(1, walletService.getWallets().size());
	}

	@Test @DisplayName("addWallet saves wallet")
	void addWallet() {
		when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
		Wallet r = walletService.addWallet(wallet);
		assertNotNull(r);
	}

	@Test @DisplayName("addWallet defaults balance to 0 when null")
	void addWallet_NullBalance() {
		wallet.setCurrentBalance(null);
		when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
		walletService.addWallet(wallet);
		assertEquals(0.0, wallet.getCurrentBalance());
	}

	@Test @DisplayName("addMoney credits wallet")
	void addMoney_Ok() {
		when(walletRepository.findByWalletId(1)).thenReturn(Optional.of(wallet));
		when(statementRepository.save(any(Statement.class))).thenReturn(new Statement());
		when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

		walletService.addMoney(wallet, 200.0, "Test deposit");

		assertEquals(700.0, wallet.getCurrentBalance());
		verify(statementRepository).save(any(Statement.class));
	}

	@Test @DisplayName("addMoney throws when wallet not found")
	void addMoney_NotFound() {
		when(walletRepository.findByWalletId(99)).thenReturn(Optional.empty());
		Wallet w = new Wallet(); w.setWalletId(99);
		assertThrows(RuntimeException.class, () -> walletService.addMoney(w, 100.0, "test"));
	}

	@Test @DisplayName("update (pay) deducts balance")
	void update_Ok() {
		when(walletRepository.findByWalletId(1)).thenReturn(Optional.of(wallet));
		when(statementRepository.save(any(Statement.class))).thenReturn(new Statement());
		when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

		walletService.update(wallet, 200.0, "Payment", 10);

		assertEquals(300.0, wallet.getCurrentBalance());
	}

	@Test @DisplayName("update throws on insufficient balance")
	void update_InsufficientBalance() {
		when(walletRepository.findByWalletId(1)).thenReturn(Optional.of(wallet));
		assertThrows(RuntimeException.class, () -> walletService.update(wallet, 1000.0, "Payment", 10));
	}

	@Test @DisplayName("refund credits wallet back")
	void refund_Ok() {
		when(walletRepository.findByWalletId(1)).thenReturn(Optional.of(wallet));
		when(statementRepository.save(any(Statement.class))).thenReturn(new Statement());
		when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

		walletService.refund(wallet, 100.0, "Refund", 10);

		assertEquals(600.0, wallet.getCurrentBalance());
	}

	@Test @DisplayName("getById returns wallet")
	void getById_Ok() {
		when(walletRepository.findByWalletId(1)).thenReturn(Optional.of(wallet));
		Wallet r = walletService.getById(1);
		assertEquals(1, r.getWalletId());
	}

	@Test @DisplayName("getById throws when not found")
	void getById_NotFound() {
		when(walletRepository.findByWalletId(99)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> walletService.getById(99));
	}

	@Test @DisplayName("getStatementsById returns statements")
	void getStatementsById() {
		when(statementRepository.findByWalletWalletId(1)).thenReturn(List.of());
		List<Statement> r = walletService.getStatementsById(1);
		assertNotNull(r);
	}

	@Test @DisplayName("withdraw deducts balance")
	void withdraw_Ok() {
		when(walletRepository.findByWalletId(1)).thenReturn(Optional.of(wallet));
		when(statementRepository.save(any(Statement.class))).thenReturn(new Statement());
		when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

		walletService.withdraw(wallet, 100.0, "Withdrawal");

		assertEquals(400.0, wallet.getCurrentBalance());
	}

	@Test @DisplayName("withdraw throws on insufficient balance")
	void withdraw_Insufficient() {
		when(walletRepository.findByWalletId(1)).thenReturn(Optional.of(wallet));
		assertThrows(RuntimeException.class, () -> walletService.withdraw(wallet, 1000.0, "Withdrawal"));
	}
}
