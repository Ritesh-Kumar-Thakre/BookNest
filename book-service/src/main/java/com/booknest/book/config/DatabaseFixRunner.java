package com.booknest.book.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Fixes existing database columns that may not have proper defaults.
 * This runs once at startup and safely alters columns.
 */
@Component
public class DatabaseFixRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseFixRunner.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public void run(String... args) {
		try {
			entityManager.createNativeQuery(
				"ALTER TABLE books MODIFY COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE"
			).executeUpdate();
		} catch (Exception e) {
			// Column might already be correct — ignore
		}

		try {
			entityManager.createNativeQuery(
				"ALTER TABLE books MODIFY COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE"
			).executeUpdate();
		} catch (Exception e) {
			// Column might already be correct — ignore
		}

		try {
			entityManager.createNativeQuery(
				"ALTER TABLE books MODIFY COLUMN rating DOUBLE DEFAULT 0.0"
			).executeUpdate();
		} catch (Exception e) {
			// Column might already be correct — ignore
		}

		try {
			entityManager.createNativeQuery(
				"ALTER TABLE books MODIFY COLUMN description TEXT"
			).executeUpdate();
			log.info("Successfully altered description column to TEXT");
		} catch (Exception e) {
			log.warn("Failed to alter description column: {}", e.getMessage());
		}
	}
}
