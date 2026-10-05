package com.fitkart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    @Value("")
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }
}
