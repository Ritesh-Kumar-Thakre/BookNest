package com.booknest.book.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booknest.book.entity.BookImage;

public interface BookImageRepository extends JpaRepository<BookImage, Integer> {

	List<BookImage> findByBookIdOrderByDisplayOrderAsc(Integer bookId);

	List<BookImage> findByBookIdAndIsPrimaryTrue(Integer bookId);

	void deleteByBookId(Integer bookId);
}
