package com.ispilo.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to register a new app installation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppRegistrationRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    private String deviceName;

    @NotBlank(message = "OS version is required")
    private String osVersion;

    @NotBlank(message = "App version is required")
    private String appVersion;

    @NotBlank(message = "Platform (ANDROID/IOS) is required")
    private String platform;

    // Device fingerprint (optional)
    private String deviceFingerprint;
}
