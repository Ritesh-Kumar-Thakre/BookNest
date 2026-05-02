package com.cg.wallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Map;
import com.cg.wallet.entity.Wallet;

import com.cg.wallet.repository.WalletRepository;
import com.cg.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

	private final WalletService walletService;
	private final WalletRepository walletRepository;

	@GetMapping("/all")
	public ResponseEntity<?> getWallets() {

		return new ResponseEntity<>(

				walletService.getWallets(),

				HttpStatus.OK

		);

	}

	@PostMapping("/add")
	public ResponseEntity<?> addWallet(

			@RequestBody Wallet wallet

	) {

		return new ResponseEntity<>(

				walletService.addWallet(wallet),

				HttpStatus.CREATED

		);

	}

	@PutMapping("/addmoney/{walletId}")
	public ResponseEntity<?> addMoney(

			@PathVariable Integer walletId,

			@RequestParam Double amount,

			@RequestParam String remarks

	) {

		Wallet wallet = walletService.getById(walletId);

		walletService.addMoney(wallet, amount, remarks);

		return ResponseEntity.ok().body(java.util.Map.of("message", "Money added successfully"));

	}

	@PutMapping("/update/{walletId}")
	public ResponseEntity<?> update(

			@PathVariable Integer walletId,

			@RequestParam Double amount,

			@RequestParam String remarks,

			@RequestParam Integer orderId

	) {

		Wallet wallet = walletService.getById(walletId);

		walletService.update(wallet, amount, remarks, orderId);

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@GetMapping("/{walletId}")
	public ResponseEntity<?> getById(

			@PathVariable Integer walletId

	) {

		return new ResponseEntity<>(

				walletService.getById(walletId),

				HttpStatus.OK

		);

	}

	@GetMapping("/statement/{walletId}")
	public ResponseEntity<?> statementsById(

			@PathVariable Integer walletId

	) {

		return new ResponseEntity<>(

				walletService.getStatementsById(walletId),

				HttpStatus.OK

		);

	}

	@GetMapping("/statement")
	public ResponseEntity<?> statements() {

		return new ResponseEntity<>(

				walletService.getStatements(),

				HttpStatus.OK

		);

	}

	@DeleteMapping("/{walletId}")
	public ResponseEntity<?> delete(

			@PathVariable Integer walletId

	) {

		walletService.deleteById(walletId);

		return new ResponseEntity<>(HttpStatus.OK);

	}

	@GetMapping("/balance")
	public ResponseEntity<?> getBalance(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
		if (userId == null) {
			return new ResponseEntity<>(java.util.Map.of("message", "User not logged in"), HttpStatus.UNAUTHORIZED);
		}
		Double balance = walletRepository.findByUserId(userId)
				.map(Wallet::getCurrentBalance)
				.orElse(0.0);
		return ResponseEntity.ok(Map.of("balance", balance));
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMyWallet(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
		if (userId == null) {
			return new ResponseEntity<>(java.util.Map.of("message", "User not logged in"), HttpStatus.UNAUTHORIZED);
		}
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found"));
		return ResponseEntity.ok(wallet);
	}

	@PutMapping("/pay")
	public ResponseEntity<Map<String, Boolean>> payMoney(
			@RequestHeader("X-User-Id") Integer userId,
			@RequestParam Double amount,
			@RequestParam Integer orderId) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found"));

		walletService.update(wallet, amount, "Order payment", orderId);

		return ResponseEntity.ok(Map.of("success", true));
	}

	@PutMapping("/refund")
	public ResponseEntity<Map<String, Boolean>> refundMoney(
			@RequestHeader("X-User-Id") Integer userId,
			@RequestParam Double amount,
			@RequestParam Integer orderId) {
		Wallet wallet = walletRepository.findByUserId(userId)
				.orElseThrow(() -> new RuntimeException("Wallet not found"));

		walletService.refund(wallet, amount, "Order cancellation refund", orderId);

		return ResponseEntity.ok(Map.of("success", true));
	}
}