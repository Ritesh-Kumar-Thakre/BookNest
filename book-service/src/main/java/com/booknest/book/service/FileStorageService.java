package com.booknest.book.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class FileStorageService {

	@Value("${app.upload.dir:uploads/books}")
	private String uploadDir;

	private Path uploadPath;

	@PostConstruct
	public void init() {
		uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(uploadPath);
		} catch (IOException e) {
			throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
		}
	}

	public String storeFile(MultipartFile file) {
		String originalName = file.getOriginalFilename();
		String extension = "";
		if (originalName != null && originalName.contains(".")) {
			extension = originalName.substring(originalName.lastIndexOf("."));
		}
		String fileName = UUID.randomUUID().toString() + extension;

		try {
			Path targetLocation = uploadPath.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			return fileName;
		} catch (IOException e) {
			throw new RuntimeException("Could not store file: " + fileName, e);
		}
	}

	public Path getFilePath(String fileName) {
		return uploadPath.resolve(fileName).normalize();
	}

	public void deleteFile(String fileName) {
		try {
			Path filePath = uploadPath.resolve(fileName).normalize();
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			// Log but don't throw — file might already be deleted
		}
	}
}
