package com.ispilo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ispilo.model.dto.response.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${app.cdn-url}")
    private String cdnUrl;

    private final AmazonS3 s3Client;

    public MediaUploadResponse uploadFile(MultipartFile file, String type, String userId) {
        String fileName = generateFileName(file, userId, type);
        String fileUrl = uploadToS3(file, fileName);

        return MediaUploadResponse.builder()
                .mediaUrl(fileUrl)
                .mediaType(type)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    private String uploadToS3(MultipartFile file, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

            return String.format("%s/%s", cdnUrl, fileName);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
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
