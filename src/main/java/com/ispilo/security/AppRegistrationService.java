package com.ispilo.security;

import com.ispilo.repository.AppCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

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

            // Create app credentials
            AppCredentials credentials = AppCredentials.builder()
                    .appPrivateKey(appPrivateKey)
                    .appId(appId)
                    .deviceId(request.getDeviceId())
                    .serverPublicKey(encryptionService.publicKeyToString(serverKeyPair.getPublic()))
                    .encryptionAlgorithm("RSA-4096/AES-256/SHA-256")
                    .registeredAt(System.currentTimeMillis())
                    .isActive(true)
                    .deviceName(request.getDeviceName())
                    .osVersion(request.getOsVersion())
                    .appVersion(request.getAppVersion())
                    .platform(request.getPlatform())
                    .build();

            // Save to database
            appCredentialsRepository.save(credentials);

            log.info("App registered successfully - App ID: {}, Device: {}", appId, request.getDeviceId());

            // Return credentials to app (app private key only once!)
            return credentials;
        } catch (Exception e) {
            log.error("Error registering app", e);
            throw new RuntimeException("Failed to register app", e);
        }
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
}
