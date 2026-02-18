package com.ispilo.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSettingsRequest {

    // Security settings
    private Boolean biometricAuth;
    private Boolean twoFactorAuth;

    // Privacy settings
    private Boolean accountVisibility;
    private Boolean phonePrivacyPublic;
    private Boolean profilePrivate;

    // Notification settings
    private Boolean socialNotifications;
    private Boolean messageNotifications;
    private Boolean educationNotifications;
    private Boolean marketplaceNotifications;
    private Boolean pushNotifications;
    private Boolean emailNotifications;

    // Theme settings
    private String themeMode; // LIGHT, DARK, SYSTEM
    private Boolean highContrast;
    private Boolean largeTextEnabled;

    // Data settings
    private Boolean offlineContent;
    private Boolean autoDownloadMedia;
    private Boolean dataCollection;
}
