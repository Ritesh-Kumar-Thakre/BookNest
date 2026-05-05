package com.booknest.book.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booknest.book.entity.Book;
import com.booknest.book.repository.BookRepository;
import com.booknest.book.service.impl.BookServiceImpl;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

	@Mock
	private BookRepository bookRepository;

	@InjectMocks
	private BookServiceImpl bookService;

	private Book sampleBook;

	@BeforeEach
	void setUp() {
		sampleBook = new Book();
		sampleBook.setBookId(1);
		sampleBook.setTitle("Clean Code");
		sampleBook.setAuthor("Robert C. Martin");
		sampleBook.setIsbn("978-0132350884");
		sampleBook.setGenre("Technology");
		sampleBook.setPublisher("Pearson");
		sampleBook.setPrice(new BigDecimal("39.99"));
		sampleBook.setStock(10);
		sampleBook.setRating(4.5);
		sampleBook.setFeatured(false);
		sampleBook.setIsVerified(true);
	}

	// ──────────────────────────────────────────────
	// addBook
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("addBook()")
	class AddBookTests {

		@Test
		@DisplayName("should add book with SELLER role — not verified")
		void addBook_Seller_ShouldSetVerifiedFalse() {
			when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
			when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

			Book result = bookService.addBook(sampleBook, "SELLER");

			assertNotNull(result);
			verify(bookRepository).findByIsbn(sampleBook.getIsbn());
			verify(bookRepository).save(sampleBook);
		}

		@Test
		@DisplayName("should add book with ADMIN role — auto-verified")
		void addBook_Admin_ShouldSetVerifiedTrue() {
			when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
			when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

			bookService.addBook(sampleBook, "ADMIN");

			assertTrue(sampleBook.getIsVerified());
			verify(bookRepository).save(sampleBook);
		}

		@Test
		@DisplayName("should throw when ISBN already exists")
		void addBook_DuplicateIsbn_ShouldThrow() {
			when(bookRepository.findByIsbn(sampleBook.getIsbn())).thenReturn(Optional.of(sampleBook));

			RuntimeException ex = assertThrows(RuntimeException.class,
					() -> bookService.addBook(sampleBook, "ADMIN"));
			assertEquals("Book already exists", ex.getMessage());
			verify(bookRepository, never()).save(any());
		}

		@Test
		@DisplayName("should default featured, rating, stock when null")
		void addBook_NullDefaults() {
			sampleBook.setFeatured(null);
			sampleBook.setRating(null);
			sampleBook.setStock(null);

			when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
			when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

			bookService.addBook(sampleBook, "ADMIN");

			assertFalse(sampleBook.getFeatured());
			assertEquals(0.0, sampleBook.getRating());
			assertEquals(0, sampleBook.getStock());
		}
	}

	// ──────────────────────────────────────────────
	// getAllBooks
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("getAllBooks()")
	class GetAllBooksTests {

		@Test
		@DisplayName("should return all books")
		void getAllBooks_ShouldReturnList() {
			when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

			List<Book> result = bookService.getAllBooks();

			assertEquals(1, result.size());
			assertEquals("Clean Code", result.get(0).getTitle());
		}

		@Test
		@DisplayName("should throw when no books available")
		void getAllBooks_Empty_ShouldThrow() {
			when(bookRepository.findAll()).thenReturn(new ArrayList<>());

			RuntimeException ex = assertThrows(RuntimeException.class, () -> bookService.getAllBooks());
			assertTrue(ex.getMessage().contains("No books"));
		}
	}

	// ──────────────────────────────────────────────
	// getBookById
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("getBookById()")
	class GetBookByIdTests {

		@Test
		@DisplayName("should return book when found")
		void getBookById_Found() {
			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));

			Optional<Book> result = bookService.getBookById(1);

			assertTrue(result.isPresent());
			assertEquals("Clean Code", result.get().getTitle());
		}

		@Test
		@DisplayName("should throw when book not found")
		void getBookById_NotFound_ShouldThrow() {
			when(bookRepository.findById(99)).thenReturn(Optional.empty());

			assertThrows(RuntimeException.class, () -> bookService.getBookById(99));
		}
	}

	// ──────────────────────────────────────────────
	// searchBooks
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("searchBooks()")
	class SearchBooksTests {

		@Test
		@DisplayName("should search by keyword")
		void searchBooks_Keyword() {
			when(bookRepository.searchByKeyword("Clean")).thenReturn(List.of(sampleBook));

			List<Book> result = bookService.searchBooks("keyword", "Clean");

			assertEquals(1, result.size());
		}

		@Test
		@DisplayName("should search by author")
		void searchBooks_Author() {
			when(bookRepository.findByAuthor("Robert")).thenReturn(List.of(sampleBook));

			List<Book> result = bookService.searchBooks("author", "Robert");

			assertEquals(1, result.size());
		}

		@Test
		@DisplayName("should search by genre")
		void searchBooks_Genre() {
			when(bookRepository.findByGenreIgnoreCase("Technology")).thenReturn(List.of(sampleBook));

			List<Book> result = bookService.searchBooks("genre", "Technology");

			assertEquals(1, result.size());
		}

		@Test
		@DisplayName("should sort by rating")
		void searchBooks_Rating() {
			when(bookRepository.findAllByOrderByRatingDesc()).thenReturn(List.of(sampleBook));

			List<Book> result = bookService.searchBooks("rating", "any");

			assertFalse(result.isEmpty());
		}

		@Test
		@DisplayName("should sort by price")
		void searchBooks_Price() {
			when(bookRepository.findAllByOrderByPriceAsc()).thenReturn(List.of(sampleBook));

			List<Book> result = bookService.searchBooks("price", "any");

			assertFalse(result.isEmpty());
		}

		@Test
		@DisplayName("should throw for invalid category")
		void searchBooks_InvalidCategory_ShouldThrow() {
			assertThrows(RuntimeException.class, () -> bookService.searchBooks("unknown", "val"));
		}
	}

	// ──────────────────────────────────────────────
	// getByGenre
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("getByGenre()")
	class GetByGenreTests {

		@Test
		@DisplayName("should return books by genre")
		void getByGenre_Found() {
			when(bookRepository.findByGenreIgnoreCase("Technology")).thenReturn(List.of(sampleBook));
			when(bookRepository.findByGenreIgnoreCase("Programming")).thenReturn(new ArrayList<>());
			when(bookRepository.findByGenreIgnoreCase("Computers")).thenReturn(new ArrayList<>());

			List<Book> result = bookService.getByGenre("Technology");

			assertFalse(result.isEmpty());
		}

		@Test
		@DisplayName("should throw when no books found for genre")
		void getByGenre_NotFound_ShouldThrow() {
			when(bookRepository.findByGenreIgnoreCase("Unknown")).thenReturn(new ArrayList<>());

			assertThrows(RuntimeException.class, () -> bookService.getByGenre("Unknown"));
		}
	}

	// ──────────────────────────────────────────────
	// updateBook
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("updateBook()")
	class UpdateBookTests {

		@Test
		@DisplayName("should update book fields")
		void updateBook_Success() {
			Book updateData = new Book();
			updateData.setBookId(1);
			updateData.setTitle("Clean Code Updated");

			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));
			when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

			Book result = bookService.updateBook(updateData);

			assertNotNull(result);
			verify(bookRepository).save(any(Book.class));
		}

		@Test
		@DisplayName("should throw when book not found")
		void updateBook_NotFound_ShouldThrow() {
			Book updateData = new Book();
			updateData.setBookId(99);

			when(bookRepository.findById(99)).thenReturn(Optional.empty());

			assertThrows(RuntimeException.class, () -> bookService.updateBook(updateData));
		}
	}

	// ──────────────────────────────────────────────
	// deleteBook
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("deleteBook()")
	class DeleteBookTests {

		@Test
		@DisplayName("should delete existing book")
		void deleteBook_Success() {
			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));
			doNothing().when(bookRepository).deleteById(1);

			bookService.deleteBook(1);

			verify(bookRepository).deleteById(1);
		}

		@Test
		@DisplayName("should throw when book not found")
		void deleteBook_NotFound_ShouldThrow() {
			when(bookRepository.findById(99)).thenReturn(Optional.empty());

			assertThrows(RuntimeException.class, () -> bookService.deleteBook(99));
		}
	}

	// ──────────────────────────────────────────────
	// updateStock
	// ──────────────────────────────────────────────
	@Nested
	@DisplayName("updateStock()")
	class UpdateStockTests {

		@Test
		@DisplayName("should update stock relatively (positive)")
		void updateStock_RelativePositive() {
			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));
			when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

			bookService.updateStock(1, 5, true);

			assertEquals(15, sampleBook.getStock());
		}

		@Test
		@DisplayName("should update stock relatively (negative)")
		void updateStock_RelativeNegative() {
			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));
			when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

			bookService.updateStock(1, -3, true);

			assertEquals(7, sampleBook.getStock());
		}

		@Test
		@DisplayName("should throw on insufficient stock (relative)")
		void updateStock_InsufficientStock_ShouldThrow() {
			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));

			assertThrows(RuntimeException.class, () -> bookService.updateStock(1, -20, true));
		}

		@Test
		@DisplayName("should set stock absolutely")
		void updateStock_Absolute() {
			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));
			when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

			bookService.updateStock(1, 50, false);

			assertEquals(50, sampleBook.getStock());
		}

		@Test
		@DisplayName("should throw on negative absolute stock")
		void updateStock_NegativeAbsolute_ShouldThrow() {
			when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));

			assertThrows(RuntimeException.class, () -> bookService.updateStock(1, -1, false));
		}

		@Test
		@DisplayName("should throw when value is null")
		void updateStock_NullValue_ShouldThrow() {
			assertThrows(RuntimeException.class, () -> bookService.updateStock(1, null, true));
		}
	}

	// ──────────────────────────────────────────────
	// getFeaturedBooks & updateRating
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("getFeaturedBooks should return featured books")
	void getFeaturedBooks() {
		sampleBook.setFeatured(true);
		when(bookRepository.findByFeaturedTrue()).thenReturn(List.of(sampleBook));

		List<Book> result = bookService.getFeaturedBooks();

		assertEquals(1, result.size());
		assertTrue(result.get(0).getFeatured());
	}

	@Test
	@DisplayName("updateRating should update book rating")
	void updateRating_Success() {
		when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));
		when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

		bookService.updateRating(1, 4.8);

		assertEquals(4.8, sampleBook.getRating());
		verify(bookRepository).save(sampleBook);
	}

	@Test
	@DisplayName("updateRating should throw when book not found")
	void updateRating_NotFound_ShouldThrow() {
		when(bookRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> bookService.updateRating(99, 4.0));
	}

	// ──────────────────────────────────────────────
	// getPendingBooks & verifyBook
	// ──────────────────────────────────────────────
	@Test
	@DisplayName("getPendingBooks should return unverified books")
	void getPendingBooks() {
		Book unverified = new Book();
		unverified.setIsVerified(false);
		unverified.setTitle("Pending Book");

		Book verified = new Book();
		verified.setIsVerified(true);
		verified.setTitle("Verified Book");

		when(bookRepository.findAll()).thenReturn(List.of(unverified, verified));

		List<Book> result = bookService.getPendingBooks();

		assertEquals(1, result.size());
		assertEquals("Pending Book", result.get(0).getTitle());
	}

	@Test
	@DisplayName("verifyBook should set isVerified to true")
	void verifyBook_Success() {
		sampleBook.setIsVerified(false);
		when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook));
		when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

		bookService.verifyBook(1);

		assertTrue(sampleBook.getIsVerified());
	}

	@Test
	@DisplayName("verifyBook should throw when book not found")
	void verifyBook_NotFound_ShouldThrow() {
		when(bookRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> bookService.verifyBook(99));
	}
}
