package com.ispilo.repository;

import com.ispilo.security.AppCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for app credentials
 * Manages app registration and verification data
 */
@Repository
public interface AppCredentialsRepository extends JpaRepository<AppCredentials, String> {

    /**
     * Find app credentials by app ID
     */
    Optional<AppCredentials> findByAppId(String appId);

    /**
     * Find app credentials by device ID
     */
    Optional<AppCredentials> findByDeviceId(String deviceId);

    /**
     * Find all active apps
     */
    List<AppCredentials> findByIsActiveTrue();

    /**
     * Find apps by platform
     */
    List<AppCredentials> findByPlatform(String platform);

    /**
     * Find apps registered after a specific timestamp
     */
    List<AppCredentials> findByRegisteredAtGreaterThan(Long registeredAt);
}
