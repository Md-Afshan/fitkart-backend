package com.fitkart.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fitkart.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "cloudinary")
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private static final Map<String, Set<String>> EXTENSION_MIME_MAP = Map.of(
            ".jpg", Set.of("image/jpeg", "image/jpg"),
            ".jpeg", Set.of("image/jpeg", "image/jpg"),
            ".png", Set.of("image/png"),
            ".webp", Set.of("image/webp")
    );

    @Value("${CLOUDINARY_URL:}")
    private String cloudinaryUrl;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    private Cloudinary cloudinaryInstance;

    private synchronized Cloudinary getCloudinary() {
        if (cloudinaryInstance != null) {
            return cloudinaryInstance;
        }

        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            cloudinaryInstance = new Cloudinary(cloudinaryUrl);
            return cloudinaryInstance;
        }

        if (cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank()) {
            cloudinaryInstance = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            return cloudinaryInstance;
        }

        throw new IllegalStateException(
                "Cloudinary configuration is missing. Please configure CLOUDINARY_URL or Cloudinary API keys."
        );
    }

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

        String publicId = UUID.randomUUID().toString();

        try {
            @SuppressWarnings("rawtypes")
            Map uploadResult = getCloudinary().uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "fitkart/products",
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null || secureUrl.isBlank()) {
                throw new RuntimeException("Failed to obtain secure URL from Cloudinary response.");
            }

            return secureUrl;

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Failed to upload product image to Cloudinary.",
                    exception
            );
        }
    }

    @Override
    public void deleteProductImage(String imagePathOrUrl) {
        if (imagePathOrUrl == null || imagePathOrUrl.isBlank() || !imagePathOrUrl.contains("cloudinary.com")) {
            return;
        }

        String publicId = extractPublicIdFromUrl(imagePathOrUrl);
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            getCloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
            // Non-critical background cleanup exception
        }
    }

    private String extractPublicIdFromUrl(String url) {
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) {
            return null;
        }

        String pathAfterUpload = url.substring(uploadIndex + "/upload/".length());

        // Remove optional version prefix e.g. v1234567890/
        if (pathAfterUpload.matches("^v\\d+/.*")) {
            pathAfterUpload = pathAfterUpload.substring(pathAfterUpload.indexOf('/') + 1);
        }

        // Remove file extension
        int lastDotIndex = pathAfterUpload.lastIndexOf('.');
        if (lastDotIndex != -1) {
            pathAfterUpload = pathAfterUpload.substring(0, lastDotIndex);
        }

        return pathAfterUpload;
    }
}
