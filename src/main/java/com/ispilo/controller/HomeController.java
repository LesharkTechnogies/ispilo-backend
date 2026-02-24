package com.ispilo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> status = new HashMap<>();
        status.put("application", "Ispilo Backend API");
        status.put("status", "active");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "1.0.0");
        status.put("message", "Welcome to Ispilo API. Use /api/v1/... for endpoints.");
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
