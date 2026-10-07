package com.fitkart.service.impl;

import com.fitkart.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private static final Map<String, Set<String>> EXTENSION_MIME_MAP = Map.of(
            ".jpg", Set.of("image/jpeg", "image/jpg"),
            ".jpeg", Set.of("image/jpeg", "image/jpg"),
            ".png", Set.of("image/png"),
            ".webp", Set.of("image/webp")
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String storeProductImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required and cannot be empty.");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Invalid image file name.");
        }

        String cleanedFileName = StringUtils.cleanPath(originalFileName);

        int extensionIndex = cleanedFileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            throw new IllegalArgumentException("File must have a valid image extension (.jpg, .jpeg, .png, .webp).");
        }

        String extension = cleanedFileName.substring(extensionIndex).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported image format: " + extension + ". Allowed formats are JPG, JPEG, PNG, and WebP."
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("File content type could not be determined.");
        }

        Set<String> validMimeTypes = EXTENSION_MIME_MAP.get(extension);
        if (validMimeTypes == null || !validMimeTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "File extension " + extension + " does not match declared content type " + contentType + "."
            );
        }

        String fileName = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(fileName).normalize();

            // Prevent path traversal outside the upload directory
            if (!targetLocation.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Security violation: target path outside upload directory.");
            }

            file.transferTo(targetLocation);

            return "/uploads/products/" + fileName;

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Failed to store product image.",
                    exception
            );
        }
    }
}