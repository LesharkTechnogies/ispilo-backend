package com.ispilo.security;

import lombok.Data;

/**
 * Payload to update missing or stale app metadata after initial registration.
 * All fields are optional; only non-blank values will be applied.
 */
@Data
public class AppMetadataUpdateRequest {
    private String appId;
    private String appPrivateKey; // to prove ownership

    private String deviceName;
    private String osVersion;
    private String appVersion;
    private String platform;
    private String ipAddress;
    private String deviceFingerprint;
    private String phone;
}
