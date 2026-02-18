package com.ispilo.security;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * App credentials for device/app identification
 * Each app installed gets a unique 16-digit private key
 */
@Entity
@Table(name = "app_credentials", indexes = {
    @Index(name = "idx_app_id", columnList = "app_id"),
    @Index(name = "idx_device_id", columnList = "device_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // 16-digit unique private key for this app instance
    @Column(name = "app_private_key", nullable = false, unique = true, length = 16)
    private String appPrivateKey;

    // Unique app ID (UUID format)
    @Column(name = "app_id", nullable = false, unique = true)
    private String appId;

    // Device identifier
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    // Server's public key for client to encrypt messages
    @Column(name = "server_public_key", length = 2048)
    private String serverPublicKey;

    // Algorithm used for encryption/decryption
    @Column(name = "encryption_algorithm")
    private String encryptionAlgorithm; // RSA, AES, etc.

    // Timestamp when app was installed/registered
    @Column(name = "registered_at")
    private Long registeredAt;

    // Whether this app is active/trusted
    @Column(name = "is_active")
    private Boolean isActive;

    // Device metadata
    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "app_version")
    private String appVersion;

    @Column(name = "platform")
    private String platform; // ANDROID, IOS

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
