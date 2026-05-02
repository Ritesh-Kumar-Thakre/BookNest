package com.cg.wallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.wallet.entity.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Integer> {

	Optional<Wallet> findByWalletId(Integer walletId);

	Optional<Wallet> findByUserId(Integer userId);

	void deleteByWalletId(Integer walletId);

}