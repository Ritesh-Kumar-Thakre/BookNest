package com.booknest.book.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.booknest.book.entity.Book;
import com.booknest.book.repository.BookRepository;
import com.booknest.book.service.BookService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
	private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);
	private final BookRepository bookRepository;

	@Override
	public Book addBook(Book book, String role) {
		if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
			throw new RuntimeException("Book already exists");
		}
		
		// If a SELLER adds a book, it requires verification
		if ("SELLER".equalsIgnoreCase(role)) {
			book.setIsVerified(false);
		} else {
			// Admins bypass verification
			book.setIsVerified(true);
		}
		
		// Ensure required fields are never null to prevent DB constraint violations
		if (book.getFeatured() == null) book.setFeatured(false);
		if (book.getRating() == null) book.setRating(0.0);
		if (book.getStock() == null) book.setStock(0);
		
		Book saved = bookRepository.save(book);
		log.info("Book added: id={}, title={}, role={}", saved.getBookId(), saved.getTitle(), role);
		return saved;
	}

	@Override
	public List<Book> getPendingBooks() {
		return bookRepository.findAll().stream().filter(b -> Boolean.FALSE.equals(b.getIsVerified())).toList();
	}

	@Override
	public void verifyBook(Integer bookId) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new RuntimeException("Book not found"));
		book.setIsVerified(true);
		bookRepository.save(book);
	}

	@Override
	public List<Book> getAllBooks() {
		List<Book> books = new ArrayList<>();
		books.addAll(bookRepository.findAll());
		if (books.isEmpty()) {
			throw new RuntimeException("No books avlable");
		}
		return books;
	}

	@Override
	public Optional<Book> getBookById(Integer bookId) {
		Optional<Book> bookWant = bookRepository.findById(bookId);
		if (!bookWant.isPresent()) {
			throw new RuntimeException("Book Not Found");
		}
		return bookWant;
	}

	@Override
	public List<Book> searchBooks(String catogry, String value) {
		List<Book> books = new ArrayList<>();
		switch (catogry) {
		case "keyword":
			String kw = value.toLowerCase();
			books.addAll(bookRepository.searchByKeyword(value));
			
			// Custom sort: 
			// 1. Starts with keyword (Title)
			// 2. Contains keyword (Title)
			// 3. Match in Author only
			books.sort((a, b) -> {
				String t1 = a.getTitle().toLowerCase();
				String t2 = b.getTitle().toLowerCase();
				
				// Score 0: Title starts with keyword
				// Score 1: A word inside title starts with keyword
				// Score 2: Match in author
				int score1 = t1.startsWith(kw) ? 0 : (t1.contains(" " + kw) ? 1 : 2);
				int score2 = t2.startsWith(kw) ? 0 : (t2.contains(" " + kw) ? 1 : 2);
				
				if (score1 != score2) return score1 - score2;
				return t1.compareTo(t2); // Alphabetical fallback
			});
			
			log.info("Keyword search for '{}' returned {} results with custom sorting", value, books.size());
			break;
		case "author":
			books.addAll(bookRepository.findByAuthor(value));
			break;
		case "rating":
			books.addAll(bookRepository.findAllByOrderByRatingDesc());
			break;
		case "price":
			books.addAll(bookRepository.findAllByOrderByPriceAsc());
			break;
		case "genre":
			books.addAll(bookRepository.findByGenreIgnoreCase(value));
			break;
		default:
			throw new RuntimeException("Invalid Catogry");
		}
		return books;
	}

	@Override
	public List<Book> getByGenre(String genre) {
		List<Book> bookWant = new ArrayList<>();
		bookWant.addAll(bookRepository.findByGenreIgnoreCase(genre));
		
		// Add related books for certain genres as requested
		if (genre.equalsIgnoreCase("Technology")) {
			bookWant.addAll(bookRepository.findByGenreIgnoreCase("Programming"));
			bookWant.addAll(bookRepository.findByGenreIgnoreCase("Computers"));
		} else if (genre.equalsIgnoreCase("Science")) {
			bookWant.addAll(bookRepository.findByGenreIgnoreCase("Physics"));
			bookWant.addAll(bookRepository.findByGenreIgnoreCase("Chemistry"));
		}

		if (bookWant.isEmpty()) {
			throw new RuntimeException("No books of " + genre + " genre");
		}
		return bookWant;
	}

	@Override
	public Book updateBook(Book book) {
		Optional<Book> bookU = bookRepository.findById(book.getBookId());

		if (!bookU.isPresent()) {
			throw new RuntimeException("Book not found");
		}

		Book updateBook = bookU.get();
		if (book.getTitle() != null) {
			updateBook.setTitle(book.getTitle());
		}
		if (book.getAuthor() != null) {
			updateBook.setAuthor(book.getAuthor());
		}
		if (book.getIsbn() != null) {
			updateBook.setIsbn(book.getIsbn());
		}
		if (book.getPublisher() != null) {
			updateBook.setPublisher(book.getPublisher());
		}

		if (book.getPrice() != null) {
			updateBook.setPrice(book.getPrice());
		}

		if (book.getStock() != null) {
			updateBook.setStock(book.getStock());
		}
		if (book.getRating() != null) {
			updateBook.setRating(book.getRating());
		}
		if (book.getDescription() != null) {
			updateBook.setDescription(book.getDescription());
		}
		if (book.getCoverImageUrl() != null) {
			updateBook.setCoverImageUrl(book.getCoverImageUrl());
		}
		if (book.getGenre() != null) {
			updateBook.setGenre(book.getGenre());
		}
		if (book.getFeatured() != null) {
			updateBook.setFeatured(book.getFeatured());
		}
		if (book.getIsVerified() != null) {
			updateBook.setIsVerified(book.getIsVerified());
		}
		return bookRepository.save(updateBook);
	}

	@Override
	public void deleteBook(Integer bookId) {
		if (!bookRepository.findById(bookId).isPresent()) {
			throw new RuntimeException("No book present of id" + " " + bookId);
		}
		bookRepository.deleteById(bookId);
		log.info("Book deleted: bookId={}", bookId);
	}

	@Override
	public void updateStock(Integer bookId, Integer value, boolean isRelative) {
		if (value == null) throw new RuntimeException("Stock value or change cannot be null");
		
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new RuntimeException("No such book present"));

		if (isRelative) {
			int newStock = book.getStock() + value;
			if (newStock < 0) {
				throw new RuntimeException("Insufficient stock for bookId: " + bookId);
			}
			book.setStock(newStock);
			log.info("Stock updated relatively: bookId={}, change={}, newStock={}", bookId, value, newStock);
		} else {
			if (value < 0) {
				throw new RuntimeException("Invalid Stock value: " + value);
			}
			book.setStock(value);
			log.info("Stock updated absolutely: bookId={}, newStock={}", bookId, value);
		}
		
		bookRepository.save(book);
	}

	@Override
	public List<Book> getFeaturedBooks() {
		return bookRepository.findByFeaturedTrue();
	}

	@Override
	public void updateRating(Integer bookId, Double rating) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
		book.setRating(rating);
		bookRepository.save(book);
	}
}
