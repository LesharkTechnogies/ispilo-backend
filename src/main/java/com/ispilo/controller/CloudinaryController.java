package com.ispilo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cloudinary")
public class CloudinaryController {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @GetMapping
    public ResponseEntity<Map<String, String>> getCloudinaryConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("cloudName", cloudName);
        config.put("apiKey", apiKey);
        config.put("apiSecret", apiSecret);
        
        return ResponseEntity.ok(config);
    }
}
