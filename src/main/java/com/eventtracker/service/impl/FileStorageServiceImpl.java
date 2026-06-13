package com.eventtracker.service.impl;

import com.eventtracker.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    // Folder on disk (relative to the app's working directory). Configurable.
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            // Keep the original extension, give the file a unique name
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.')).toLowerCase();
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // Web-accessible path (served by WebConfig resource handler)
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store uploaded image: " + e.getMessage(), e);
        }
    }
}
