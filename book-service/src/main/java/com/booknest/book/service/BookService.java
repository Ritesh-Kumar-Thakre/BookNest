package com.booknest.book.service;

import java.util.List;
import java.util.Optional;

import com.booknest.book.dto.request.AddBookRequest;
import com.booknest.book.entity.Book;

public interface BookService {
	Book addBook(Book book, String role);

	List<Book> getAllBooks();

	Optional<Book> getBookById(Integer bookId);

	List<Book> searchBooks(String catogry,String value);

	List<Book> getByGenre(String genre);

	Book updateBook(Book book);

	void deleteBook(Integer bookId);

	void updateStock(Integer bookId, Integer stock, boolean isRelative);

	List<Book> getFeaturedBooks();

	void updateRating(Integer bookId, Double rating);

	List<Book> getPendingBooks();

	void verifyBook(Integer bookId);
}
