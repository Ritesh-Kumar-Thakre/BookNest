package com.booknest.book.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.booknest.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Integer> {

	List<Book> findByTitle(String title);

	List<Book> findByAuthor(String author);

	List<Book> findByGenre(String genra);

	List<Book> findByGenreIgnoreCase(String genre);

	Optional<Book> findByIsbn(String isbn);

	List<Book> findByPriceBetween(double min, double max);

	List<Book> findByStockGreaterThan(Integer minStock);

	@Query("""
			SELECT b FROM Book b
			WHERE lower(b.title) LIKE lower(concat(:keyword, '%'))
			OR lower(b.title) LIKE lower(concat('% ', :keyword, '%'))
			OR lower(b.author) LIKE lower(concat(:keyword, '%'))
			OR lower(b.author) LIKE lower(concat('% ', :keyword, '%'))
			""")
	List<Book> searchByKeyword(String keyword);

	List<Book> findByFeaturedTrue();

	List<Book> findAllByOrderByRatingDesc();

	List<Book> findAllByOrderByPriceAsc();
}