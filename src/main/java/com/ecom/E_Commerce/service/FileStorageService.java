package com.ecom.E_Commerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads");
    private final Path staticUploadDir = Paths.get("src/main/resources/static/uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(uploadDir);
            Files.createDirectories(staticUploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;
            // Save to uploads/ (persistent)
            Path target1 = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target1, StandardCopyOption.REPLACE_EXISTING);
            // Also save to static/uploads for immediate serving
            Path target2 = staticUploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target2, StandardCopyOption.REPLACE_EXISTING);
            // Fallback if second copy fails due to stream closed, copy from first
            if (!Files.exists(target2)) {
                Files.copy(target1, target2, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
