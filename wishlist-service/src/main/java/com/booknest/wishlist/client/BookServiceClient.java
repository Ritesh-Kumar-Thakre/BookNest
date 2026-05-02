package com.booknest.wishlist.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.booknest.wishlist.dto.response.BookResponse;

@FeignClient(name = "book-service")
public interface BookServiceClient {

	@GetMapping("/books/{bookId}")
	BookResponse getBookById(@PathVariable Integer bookId);
}
