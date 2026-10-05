package com.fitkart.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ProductImageFileController {

    private final Path uploadPath;

    public ProductImageFileController(
            @Value("${file.upload-dir}") String uploadDir
    ) {
        this.uploadPath = Paths
                .get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @GetMapping("/uploads/products/{filename:.+}")
    public ResponseEntity<Resource> serveProductImage(
            @PathVariable String filename
    ) throws IOException {

        Path filePath = uploadPath
                .resolve(filename)
                .normalize();

        // Prevent path traversal outside the upload directory
        if (!filePath.startsWith(uploadPath)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(
                filePath.toUri()
        );

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);

        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }
}