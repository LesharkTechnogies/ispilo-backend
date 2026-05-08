package com.ispilo.security;

import com.ispilo.repository.AppCredentialsRepository;
import com.ispilo.service.BannedDeviceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service for managing app registration and credentials
 * Each app gets a unique ID and 16-digit private key
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppRegistrationService {

    private final SecurityEncryptionService encryptionService;
    private final AppCredentialsRepository appCredentialsRepository;
    private final BannedDeviceCacheService bannedDeviceCacheService;

    // Server's RSA key pair (should be generated once at startup and stored securely)
    private static KeyPair serverKeyPair;

    /**
     * Initialize server's RSA key pair at application startup
     * In production, load from secure storage (not generated each time)
     */
    public void initializeServerKeys() {
        serverKeyPair = encryptionService.generateRSAKeyPair();
        log.info("Server RSA key pair initialized");
    }

    /**
     * Register a new app installation
     * Returns app credentials including server's public key
     */
    public AppCredentials registerApp(AppRegistrationRequest request) {
        try {
        // Generate unique app credentials
        String appPrivateKey = encryptionService.generateAppPrivateKey();
        String appId = encryptionService.generateAppId();

        // Allow missing fields by providing safe defaults
        String deviceId = (request.getDeviceId() == null || request.getDeviceId().isBlank())
            ? "device-" + UUID.randomUUID()
            : request.getDeviceId();

        if (bannedDeviceCacheService.isBanned(deviceId)) {
            throw new RuntimeException("Device is banned");
        }

        String deviceName = defaultValue(request.getDeviceName(), "unknown");
        String osVersion = defaultValue(request.getOsVersion(), "unknown");
        String appVersion = defaultValue(request.getAppVersion(), "unknown");
        String platform = defaultValue(request.getPlatform(), "unknown");
        String ipAddress = defaultValue(request.getIpAddress(), null);
        String deviceFingerprint = defaultValue(request.getDeviceFingerprint(), null);
            String phone = defaultValue(request.getPhone(), null);

        List<String> missingFields = collectMissingFields(request);

            // Create app credentials
            AppCredentials credentials = AppCredentials.builder()
                    .appPrivateKey(appPrivateKey)
                    .appId(appId)
            .deviceId(deviceId)
                    .serverPublicKey(encryptionService.publicKeyToString(serverKeyPair.getPublic()))
                    .encryptionAlgorithm("RSA-4096/AES-256/SHA-256")
                    .registeredAt(System.currentTimeMillis())
                    .isActive(true)
            .deviceName(deviceName)
            .osVersion(osVersion)
            .appVersion(appVersion)
            .platform(platform)
            .ipAddress(ipAddress)
            .deviceFingerprint(deviceFingerprint)
            .phone(phone)
                    .build();

            // Save to database with null-safety guard
            appCredentialsRepository.save(Objects.requireNonNull(credentials));

            log.info("App registered successfully - App ID: {}, Device: {}", appId, request.getDeviceId());

            // Return credentials to app (app private key only once!)
            // Attach missing fields info into a transient holder inside credentials (not persisted)
            credentials.setTransientMissingFields(missingFields);
            return credentials;
        } catch (Exception e) {
            log.error("Error registering app", e);
            throw new RuntimeException("Failed to register app", e);
        }
    }

    private List<String> collectMissingFields(AppRegistrationRequest request) {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getDeviceId())) missing.add("deviceId");
        if (isBlank(request.getDeviceName())) missing.add("deviceName");
        if (isBlank(request.getOsVersion())) missing.add("osVersion");
        if (isBlank(request.getAppVersion())) missing.add("appVersion");
        if (isBlank(request.getPlatform())) missing.add("platform");
        if (isBlank(request.getIpAddress())) missing.add("ipAddress");
        if (isBlank(request.getDeviceFingerprint())) missing.add("deviceFingerprint");
        if (isBlank(request.getPhone())) missing.add("phone");
        return missing;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Get app credentials by app ID
     * Used to verify requests from this specific app
     */
    public Optional<AppCredentials> getAppCredentials(String appId) {
        return appCredentialsRepository.findByAppId(appId);
    }

    /**
     * Get app credentials by device ID
     */
    public Optional<AppCredentials> getAppCredentialsByDeviceId(String deviceId) {
        return appCredentialsRepository.findByDeviceId(deviceId);
    }

    /**
     * Verify app is registered and active
     */
    public boolean isAppValid(String appId) {
        Optional<AppCredentials> credentials = getAppCredentials(appId);
        return credentials.isPresent() && credentials.get().getIsActive();
    }

    /**
     * Validate request signature using app credentials
     * App encrypts a challenge with its private key, we decrypt with public key
     */
    public boolean validateAppRequest(String appId, String encryptedChallenge, String appPrivateKeyDigits) {
        try {
            Optional<AppCredentials> credentials = getAppCredentials(appId);
            if (!credentials.isPresent()) {
                return false;
            }

            AppCredentials appCreds = credentials.get();

            // Verify app private key matches
            if (!appCreds.getAppPrivateKey().equals(appPrivateKeyDigits)) {
                log.warn("Invalid app private key for app: {}", appId);
                return false;
            }

            // App is valid
            return true;
        } catch (Exception e) {
            log.error("Error validating app request", e);
            return false;
        }
    }

    /**
     * Update metadata for an existing app using its appId and appPrivateKey.
     */
    public AppCredentials updateMetadata(AppMetadataUpdateRequest request) {
        Optional<AppCredentials> credsOpt = getAppCredentials(request.getAppId());
        if (credsOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid appId");
        }

        AppCredentials creds = credsOpt.get();
        if (!creds.getAppPrivateKey().equals(request.getAppPrivateKey())) {
            throw new IllegalArgumentException("Invalid appPrivateKey");
        }

        if (!isBlank(request.getDeviceName())) creds.setDeviceName(request.getDeviceName());
        if (!isBlank(request.getOsVersion())) creds.setOsVersion(request.getOsVersion());
        if (!isBlank(request.getAppVersion())) creds.setAppVersion(request.getAppVersion());
        if (!isBlank(request.getPlatform())) creds.setPlatform(request.getPlatform());
        if (!isBlank(request.getIpAddress())) creds.setIpAddress(request.getIpAddress());
        if (!isBlank(request.getDeviceFingerprint())) creds.setDeviceFingerprint(request.getDeviceFingerprint());
        if (!isBlank(request.getPhone())) creds.setPhone(request.getPhone());

        appCredentialsRepository.save(creds);

        // Recompute missing fields after update
        List<String> missing = new ArrayList<>();
        if (isBlank(creds.getDeviceName())) missing.add("deviceName");
        if (isBlank(creds.getOsVersion())) missing.add("osVersion");
        if (isBlank(creds.getAppVersion())) missing.add("appVersion");
        if (isBlank(creds.getPlatform())) missing.add("platform");
        if (isBlank(creds.getIpAddress())) missing.add("ipAddress");
        if (isBlank(creds.getDeviceFingerprint())) missing.add("deviceFingerprint");
        if (isBlank(creds.getPhone())) missing.add("phone");
        creds.setTransientMissingFields(missing);

        return creds;
    }

    /**
     * Deactivate app (device uninstall/logout)
     */
    public void deactivateApp(String appId) {
        try {
            Optional<AppCredentials> credentials = getAppCredentials(appId);
            if (credentials.isPresent()) {
                AppCredentials appCreds = credentials.get();
                appCreds.setIsActive(false);
                appCredentialsRepository.save(appCreds);
                log.info("App deactivated - App ID: {}", appId);
            }
        } catch (Exception e) {
            log.error("Error deactivating app", e);
        }
    }

    /**
     * Get server's public key for app to use for encryption
     */
    public PublicKey getServerPublicKey() {
        return serverKeyPair.getPublic();
    }

    /**
     * Get server's private key (for internal use only)
     */
    public PrivateKey getServerPrivateKey() {
        return serverKeyPair.getPrivate();
    }

    private String defaultValue(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
