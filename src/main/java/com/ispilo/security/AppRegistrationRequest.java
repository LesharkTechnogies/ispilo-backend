package com.ispilo.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to register a new app installation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRegistrationRequest {

    // Device ID (preferred). If missing, backend will generate a fallback.
    private String deviceId;

    // Optional metadata; missing values should not block registration.
    private String deviceName;
    private String osVersion;
    private String appVersion;
    private String platform; // ANDROID, IOS, WEB, etc.

    // Device fingerprint (optional)
    private String deviceFingerprint;

    // Client-reported IP address (optional; missing should not block registration)
    private String ipAddress;

    // User phone (optional at registration; can be supplied later)
    private String phone;
}
