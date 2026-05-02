package com.cg.order.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.cg.order.dto.response.BookResponse;

@FeignClient(name = "book-service")
public interface BookServiceClient {

	@GetMapping("/books/{bookId}")
	BookResponse getBookById(@PathVariable Integer bookId);

	// Use POST instead of PATCH — default Feign HttpURLConnection doesn't support PATCH
	@PostMapping("/books/updateStock")
	Map<String, String> updateStock(@RequestBody Map<String, String> request);

}