package com.fitkart.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeProductImage(MultipartFile file);

    void deleteProductImage(String imagePathOrUrl);
}
