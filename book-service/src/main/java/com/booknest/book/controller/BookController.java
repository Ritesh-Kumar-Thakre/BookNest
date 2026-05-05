package com.booknest.book.controller;


import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.booknest.book.entity.Book;
import com.booknest.book.entity.BookImage;
import com.booknest.book.repository.BookImageRepository;
import com.booknest.book.service.BookService;
import com.booknest.book.service.FileStorageService;

@RestController
@Slf4j
@RequestMapping("/books")
public class BookController {

	@Autowired
	private BookService bookService;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private BookImageRepository bookImageRepository;

	@PostMapping("/addBook")
	public ResponseEntity<?> addBook(@RequestBody Book book, @RequestHeader(value = "X-User-Role", required = false) String role) {
		try {
			return new ResponseEntity<>(bookService.addBook(book, role), HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/pending")
	public ResponseEntity<?> getPendingBooks() {
		try {
			return new ResponseEntity<>(bookService.getPendingBooks(), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}
	}

	@PatchMapping("/{bookId}/verify")
	public ResponseEntity<?> verifyBook(@PathVariable Integer bookId) {
		try {
			bookService.verifyBook(bookId);
			return new ResponseEntity<>(java.util.Map.of("message", "Book Verified Successfully"), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllBooks() {
		try {
			return new ResponseEntity<>(bookService.getAllBooks(), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/{bookId}")
	public ResponseEntity<?> getBookById(@PathVariable Integer bookId) {
		try {
			return new ResponseEntity<>(bookService.getBookById(bookId), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/search")
	public ResponseEntity<?> searchBooks(@RequestParam String category, @RequestParam String value) {
		return ResponseEntity.ok(bookService.searchBooks(category, value));
	}

	@GetMapping("/genre/{genre}")
	public ResponseEntity<?> getByGenre(@PathVariable String genre) {
		try {
			return new ResponseEntity<>(bookService.getByGenre(genre), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.NOT_FOUND);
		}
	}

	@PatchMapping("/update")
	public ResponseEntity<?> updateBook(@RequestBody Book book) {
		try {
			return new ResponseEntity<>(bookService.updateBook(book), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/delete/{bookId}")
	public ResponseEntity<?> deleteBook(@PathVariable Integer bookId) {
		try {
			bookService.deleteBook(bookId);
			return new ResponseEntity<>(java.util.Map.of("message", "Book Deleted Successfully"), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.NOT_FOUND);
		}
	}

	@PatchMapping("/updateStock")
	public ResponseEntity<?> updateStock(@RequestBody Map<String, String> request) {
		return doUpdateStock(request);
	}

	// POST alias for Feign inter-service calls (HttpURLConnection doesn't support PATCH)
	@PostMapping("/updateStock")
	public ResponseEntity<?> updateStockPost(@RequestBody Map<String, String> request) {
		return doUpdateStock(request);
	}

	private ResponseEntity<?> doUpdateStock(Map<String, String> request) {
		log.info("Stock update request received: {}", request);
		try {
			if (request.get("bookId") == null) throw new RuntimeException("bookId is missing");
			Integer bookId = Integer.parseInt(request.get("bookId"));
			
			if (request.containsKey("change")) {
				Integer change = Integer.parseInt(request.get("change"));
				log.info("Applying relative stock change: bookId={}, change={}", bookId, change);
				bookService.updateStock(bookId, change, true);
			} else if (request.containsKey("stock")) {
				Integer stock = Integer.parseInt(request.get("stock"));
				log.info("Setting absolute stock: bookId={}, stock={}", bookId, stock);
				bookService.updateStock(bookId, stock, false);
			} else {
				throw new RuntimeException("Neither 'change' nor 'stock' provided in request");
			}
			
			return new ResponseEntity<>(java.util.Map.of("message", "Stock Updated Successfully"), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/featured")
	public ResponseEntity<?> getFeaturedBooks() {
		try {
			return new ResponseEntity<>(bookService.getFeaturedBooks(), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.NOT_FOUND);
		}
	}

	// Internal endpoint — called by review-service to update book rating
	@PatchMapping("/{bookId}/rating")
	public ResponseEntity<?> updateRating(@PathVariable Integer bookId, @RequestParam Double rating) {
		return doUpdateRating(bookId, rating);
	}

	// POST alias for Feign inter-service calls (HttpURLConnection doesn't support PATCH)
	@PostMapping("/{bookId}/rating")
	public ResponseEntity<?> updateRatingPost(@PathVariable Integer bookId, @RequestParam Double rating) {
		return doUpdateRating(bookId, rating);
	}

	private ResponseEntity<?> doUpdateRating(Integer bookId, Double rating) {
		try {
			bookService.updateRating(bookId, rating);
			return new ResponseEntity<>(java.util.Map.of("message", "Rating updated"), HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(java.util.Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error"), HttpStatus.BAD_REQUEST);
		}
	}

	// ========================
	// Image Upload Endpoints
	// ========================

	@PostMapping("/upload-image/{bookId}")
	public ResponseEntity<?> uploadImage(
			@PathVariable Integer bookId,
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) {
		try {
			String fileName = fileStorageService.storeFile(file);
			String imageUrl = "/books/uploads/" + fileName;

			// If this is primary, clear other primary flags
			if (isPrimary) {
				List<BookImage> existingPrimary = bookImageRepository.findByBookIdAndIsPrimaryTrue(bookId);
				existingPrimary.forEach(img -> {
					img.setIsPrimary(false);
					bookImageRepository.save(img);
				});

				// Also update book's coverImageUrl
				bookService.getBookById(bookId).ifPresent(book -> {
					book.setCoverImageUrl(imageUrl);
					bookService.updateBook(book);
				});
			}

			int order = bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId).size();

			BookImage bookImage = BookImage.builder()
					.bookId(bookId)
					.imageUrl(imageUrl)
					.isPrimary(isPrimary)
					.displayOrder(order)
					.build();

			bookImage = bookImageRepository.save(bookImage);

			return ResponseEntity.ok(Map.of(
					"imageId", bookImage.getId(),
					"imageUrl", imageUrl,
					"isPrimary", isPrimary
			));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(Map.of("message", e.getMessage() != null ? e.getMessage() : "Upload failed"), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/images/{bookId}")
	public ResponseEntity<?> getBookImages(@PathVariable Integer bookId) {
		return ResponseEntity.ok(bookImageRepository.findByBookIdOrderByDisplayOrderAsc(bookId));
	}

	@DeleteMapping("/images/delete/{imageId}")
	public ResponseEntity<?> deleteBookImage(@PathVariable Integer imageId) {
		try {
			BookImage img = bookImageRepository.findById(imageId)
					.orElseThrow(() -> new RuntimeException("Image not found"));
			String fileName = img.getImageUrl().replace("/books/uploads/", "");
			fileStorageService.deleteFile(fileName);
			bookImageRepository.delete(img);
			return ResponseEntity.ok(Map.of("message", "Image deleted"));
		} catch (RuntimeException e) {
			return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("/uploads/{fileName:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
		try {
			Path filePath = fileStorageService.getFilePath(fileName);
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists() && resource.isReadable()) {
				String contentType = "application/octet-stream";
				if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) contentType = "image/jpeg";
				else if (fileName.endsWith(".png")) contentType = "image/png";
				else if (fileName.endsWith(".gif")) contentType = "image/gif";
				else if (fileName.endsWith(".webp")) contentType = "image/webp";

				return ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
						.body(resource);
			}
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
