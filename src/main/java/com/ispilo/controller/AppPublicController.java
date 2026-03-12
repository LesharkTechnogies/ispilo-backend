package com.ispilo.controller;

import com.ispilo.security.AppCredentials;
import com.ispilo.security.AppRegistrationRequest;
import com.ispilo.security.AppRegistrationService;
import com.ispilo.security.AppMetadataUpdateRequest;
import com.ispilo.security.SecurityEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Public API v1 endpoints for app registration and version info (browser-friendly).
 */
@RestController
@RequestMapping({"/api/v1", "/api", "/api/v2"})
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
@Slf4j
public class AppPublicController {

    private final AppRegistrationService appRegistrationService;
    private final SecurityEncryptionService encryptionService;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.build:1}")
    private String appBuild;

    /**
     * Browser-accessible registration endpoint expected by web clients.
     */
    @PostMapping("/registerApp")
    public ResponseEntity<?> registerApp(@RequestBody AppRegistrationRequest request) {
        try {
            log.info("[v1] Register app (web) deviceId={}, platform={}", request.getDeviceId(), request.getPlatform());
            AppCredentials credentials = appRegistrationService.registerApp(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appId", credentials.getAppId());
            response.put("appPrivateKey", credentials.getAppPrivateKey());
            response.put("serverPublicKey", credentials.getServerPublicKey());
            response.put("encryptionAlgorithm", credentials.getEncryptionAlgorithm());
            response.put("registeredAt", credentials.getRegisteredAt());
            response.put("pendingData", credentials.getTransientMissingFields());
            response.put("pendingDataCount", credentials.getTransientMissingFields() == null ? 0 : credentials.getTransientMissingFields().size());
            response.put("message", "App registered successfully. Store appPrivateKey securely!");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("[v1] Error registering app", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register app: " + e.getMessage()));
        }
    }

    /**
     * Update missing metadata later (e.g., phone/ip/fingerprint) once available from the app.
     */
    @PostMapping("/app/metadata")
    public ResponseEntity<?> updateMetadata(@RequestBody AppMetadataUpdateRequest request) {
        try {
            AppCredentials creds = appRegistrationService.updateMetadata(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appId", creds.getAppId());
            response.put("pendingData", creds.getTransientMissingFields());
            response.put("pendingDataCount", creds.getTransientMissingFields() == null ? 0 : creds.getTransientMissingFields().size());
            response.put("message", "Metadata updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception e) {
            log.error("[v1] Error updating metadata", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update metadata"));
        }
    }

    /**
     * Returns current backend version/build so web client can compare.
     */
    @GetMapping("/app/version")
    public ResponseEntity<?> getVersion() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", appVersion);
        response.put("build", appBuild);
        response.put("name", "ispilo-backend");
        response.put("message", "Version info fetched successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Public key fetch for web clients (optional helper).
     */
    @GetMapping("/app/public-key")
    public ResponseEntity<?> getPublicKey() {
        Map<String, Object> response = new HashMap<>();
        response.put("serverPublicKey", encryptionService.publicKeyToString(appRegistrationService.getServerPublicKey()));
        response.put("encryptionAlgorithm", "RSA-4096");
        response.put("keySize", "4096 bits");
        return ResponseEntity.ok(response);
    }
}
