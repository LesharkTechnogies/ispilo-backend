package com.ispilo.controller;

import com.ispilo.security.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * App Registration and Security Controller
 * Handles app ID generation, encryption key exchange, and app verification
 */
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
@Slf4j
public class AppSecurityController {

    private final AppRegistrationService appRegistrationService;
            private final SecurityEncryptionService encryptionService;

    /**
     * Register a new app installation
     * Returns: appId, appPrivateKey (16-digit), serverPublicKey
     *
     * This endpoint is called when app is first installed
     * The appPrivateKey should be stored securely in app's local storage
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerApp(@Valid @RequestBody AppRegistrationRequest request) {
        try {
            log.info("Registering new app - Device: {}, Platform: {}", request.getDeviceId(), request.getPlatform());

            // Register app and get credentials
            AppCredentials credentials = appRegistrationService.registerApp(request);

            // Response (appPrivateKey is sensitive, sent only once!)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appId", credentials.getAppId());
            response.put("appPrivateKey", credentials.getAppPrivateKey()); // 16-digit key - STORE SECURELY!
            response.put("serverPublicKey", credentials.getServerPublicKey());
            response.put("encryptionAlgorithm", credentials.getEncryptionAlgorithm());
            response.put("registeredAt", credentials.getRegisteredAt());
            response.put("message", "App registered successfully. Store appPrivateKey securely!");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error registering app", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register app: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get server's public key for message encryption
     * App uses this to encrypt messages before sending to server
     * Requires: X-App-ID and X-Device-ID headers
     */
    @GetMapping("/public-key")
    public ResponseEntity<?> getServerPublicKey(
            @RequestHeader(value = "X-App-ID", required = false) String appId,
            @RequestHeader(value = "X-Device-ID", required = false) String deviceId) {
        try {
            if (appId == null || appId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "X-App-ID header is required"));
            }

            if (!appRegistrationService.isAppValid(appId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Invalid or inactive app"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("serverPublicKey", encryptionService.publicKeyToString(appRegistrationService.getServerPublicKey()));
            response.put("encryptionAlgorithm", "RSA-4096");
            response.put("keySize", "4096 bits");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving public key", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve public key"));
        }
    }

    /**
     * Verify app is registered and active
     * Requires: X-App-ID header
     */
    @GetMapping("/verify/{appId}")
    public ResponseEntity<?> verifyApp(@PathVariable String appId) {
        try {
            boolean isValid = appRegistrationService.isAppValid(appId);

            Map<String, Object> response = new HashMap<>();
            response.put("appId", appId);
            response.put("isValid", isValid);
            response.put("status", isValid ? "ACTIVE" : "INACTIVE");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying app", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify app"));
        }
    }

    /**
     * Deactivate app (when user logs out or uninstalls)
     * Requires: X-App-ID header
     */
    @PostMapping("/deactivate/{appId}")
    public ResponseEntity<?> deactivateApp(@PathVariable String appId) {
        try {
            if (!appRegistrationService.isAppValid(appId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Invalid app ID"));
            }

            appRegistrationService.deactivateApp(appId);

            log.info("App deactivated: {}", appId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "App deactivated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deactivating app", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate app"));
        }
    }

    /**
     * Test encryption endpoint
     * App can send encrypted data to test the encryption works
     * Message should be encrypted with server's public key using RSA-4096
     */
    @PostMapping("/test-encryption")
    public ResponseEntity<?> testEncryption(
            @RequestHeader("X-App-ID") String appId,
            @RequestHeader("X-Device-ID") String deviceId,
            @RequestBody Map<String, String> payload) {
        try {
            if (!appRegistrationService.isAppValid(appId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Invalid app"));
            }

            String encryptedMessage = payload.get("encryptedMessage");
            if (encryptedMessage == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "encryptedMessage is required"));
            }

            // Decrypt message
            String decryptedMessage = encryptionService.decryptWithPrivateKey(
                    encryptedMessage,
                    appRegistrationService.getServerPrivateKey()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("decryptedMessage", decryptedMessage);
            response.put("message", "Encryption test successful!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in encryption test", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to decrypt message: " + e.getMessage()));
        }
    }
}
