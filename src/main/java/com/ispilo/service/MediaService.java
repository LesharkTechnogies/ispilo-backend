package com.ispilo.service;

import com.ispilo.model.dto.response.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {

    @Value("${app.base-url}")
    private String baseUrl;

    // Directory to store uploaded files locally
    private static final String UPLOAD_DIR = "uploads";

    public MediaUploadResponse uploadFile(MultipartFile file, String type, String userId) {
        String fileName = generateFileName(file, userId, type);
        String fileUrl = uploadToLocal(file, fileName);

        return MediaUploadResponse.builder()
                .mediaUrl(fileUrl)
                .mediaType(type)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    private String uploadToLocal(MultipartFile file, String fileName) {
        try {
            // Create the upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Resolve the full file path
            // We flatten the path structure for simplicity in local storage, replacing slashes with underscores
            String safeFileName = fileName.replace("/", "_");
            Path filePath = uploadPath.resolve(safeFileName);

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the URL to access the file
            // Assuming we have a controller to serve static files from /media/**
            return String.format("%s/media/%s", baseUrl, safeFileName);
        } catch (IOException e) {
            log.error("Failed to upload file locally", e);
            throw new RuntimeException("File upload failed");
        }
    }

    private String generateFileName(MultipartFile file, String userId, String type) {
        String extension = getFileExtension(file.getOriginalFilename());
        return String.format("%s/%s/%s%s", type, userId, UUID.randomUUID(), extension);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
