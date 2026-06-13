package com.eventtracker.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Saves an uploaded image to the uploads folder and returns a web path
     * (e.g. "/uploads/abc123.png"). Returns null if the file is empty/missing.
     */
    String storeImage(MultipartFile file);
}
