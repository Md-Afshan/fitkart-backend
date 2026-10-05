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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String storeProductImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename()
        );

        if (originalFileName.isBlank()) {
            throw new IllegalArgumentException("Invalid image file name.");
        }

        String extension = "";

        int extensionIndex = originalFileName.lastIndexOf('.');

        if (extensionIndex >= 0) {
            extension = originalFileName.substring(extensionIndex);
        }

        String fileName = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(fileName);

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