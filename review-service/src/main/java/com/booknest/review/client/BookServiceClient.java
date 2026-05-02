package com.booknest.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "book-service")
public interface BookServiceClient {

	// Use POST instead of PATCH — default Feign HttpURLConnection doesn't support PATCH
	@PostMapping("/books/{bookId}/rating")
	void updateBookRating(@PathVariable Integer bookId, @RequestParam Double rating);
}
