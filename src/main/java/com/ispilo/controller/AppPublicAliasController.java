package com.ispilo.controller;

import com.ispilo.security.AppCredentials;
import com.ispilo.security.AppRegistrationRequest;
import com.ispilo.security.AppRegistrationService;
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
 * Backward-compatible alias endpoints without version prefix.
 * These delegate to the same services as the canonical /api/v1 routes.
 */
/**
 * Deprecated: alias controller removed in favor of dual mapping on AppPublicController.
 * This class is intentionally left without Spring annotations to avoid registration.
 */
public final class AppPublicAliasController {
    private AppPublicAliasController() {}
}
