package com.softuni.finalexam.service;

import com.softuni.finalexam.exception.ProductImageStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ProductImageStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path uploadDirectory;

    public ProductImageStorageService(@Value("${app.upload.dir:uploads/products}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProductImageStorageException("product.image.required");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ProductImageStorageException("product.image.too.large");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ProductImageStorageException("product.image.invalid.type");
        }

        String extension = EXTENSIONS_BY_CONTENT_TYPE.get(contentType);
        String filename = UUID.randomUUID() + "." + extension;

        try {
            Files.createDirectories(uploadDirectory);
            Path target = uploadDirectory.resolve(filename);
            file.transferTo(target);
            log.info("Stored product image at {}", target);
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            log.error("Failed to store product image", e);
            throw new ProductImageStorageException("product.image.upload.failed");
        }
    }
}
