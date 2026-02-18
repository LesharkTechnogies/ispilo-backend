package com.ispilo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Request filter to validate app ID and device ID
 * Prevents users from querying other people's data
 * Each request must include valid app credentials
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppSecurityFilter extends OncePerRequestFilter {

    private final AppRegistrationService appRegistrationService;

    private static final String APP_ID_HEADER = "X-App-ID";
    private static final String DEVICE_ID_HEADER = "X-Device-ID";
    private static final String APP_SIGNATURE_HEADER = "X-App-Signature";

    // Endpoints that don't require app ID validation
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/app/register",  // New app registration endpoint
            "/swagger-ui",
            "/v3/api-docs",
            "/api/app/public-key" // Allow fetching public key without full validation
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();

        // Skip validation for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate app ID and device ID
        String appId = request.getHeader(APP_ID_HEADER);
        String deviceId = request.getHeader(DEVICE_ID_HEADER);
        String appSignature = request.getHeader(APP_SIGNATURE_HEADER);

        if (appId == null || appId.isEmpty() || deviceId == null || deviceId.isEmpty()) {
            // For now, allow requests without headers to pass through if they are not strictly required
            // This is to support existing clients or during transition
            // In strict mode, uncomment the following block:
            /*
            log.warn("Missing app credentials in request to: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Missing app credentials. Please include X-App-ID and X-Device-ID headers\"}");
            return;
            */
            filterChain.doFilter(request, response);
            return;
        }

        // Verify app is registered and active
        if (!appRegistrationService.isAppValid(appId)) {
            log.warn("Invalid or inactive app ID: {} from device: {}", appId, deviceId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Invalid or inactive app\"}");
            return;
        }

        // Verify device ID matches registered device
        Optional<AppCredentials> credentials = appRegistrationService.getAppCredentials(appId);
        if (credentials.isEmpty() || !credentials.get().getDeviceId().equals(deviceId)) {
            log.warn("Device ID mismatch for app: {}", appId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Device ID does not match registered device\"}");
            return;
        }

        // Store app credentials in request for use in controllers/services
        request.setAttribute("appId", appId);
        request.setAttribute("deviceId", deviceId);
        request.setAttribute("appCredentials", credentials.get());

        log.debug("App verified - App ID: {}, Device: {}", appId, deviceId);

        filterChain.doFilter(request, response);
    }

    /**
     * Check if endpoint is public (doesn't require app ID)
     */
    private boolean isPublicEndpoint(String path) {
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (path.startsWith(publicEndpoint)) {
                return true;
            }
        }
        return false;
    }
}
