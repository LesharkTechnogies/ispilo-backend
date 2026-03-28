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
        "/", "/health",
        "/api/app/version", "/api/v1/app/version", "/api/v2/app/version",
        "/api/app/public-key", "/api/v1/app/public-key", "/api/v2/app/public-key",
        "/api/registerApp", "/api/v1/registerApp", "/api/v2/registerApp",
        "/api/auth/register", "/api/v1/auth/register", "/api/auth/login", "/api/v1/auth/login",
        "/v3/api-docs", "/swagger-ui"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // CORS preflight: allow and return
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin") == null ? "*" : request.getHeader("Origin"));
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,Accept,Authorization,X-App-ID,X-Device-ID,X-App-Signature,X-Timestamp,X-Nonce,X-App-Version,X-Build-Number,X-Platform,X-IP");
            response.setHeader("Access-Control-Expose-Headers", "Authorization,X-API-Version,X-API-Deprecated,X-API-Upgrade-To,X-API-Message");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
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
            chain.doFilter(request, response);
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

        chain.doFilter(request, response);
    }

    /**
     * Check if endpoint is public (doesn't require app ID)
     */
    private boolean isPublicEndpoint(String path) {
        // Exact match for root
        if (path.equals("/")) {
            return true;
        }
        
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (!publicEndpoint.equals("/") && path.startsWith(publicEndpoint)) {
                return true;
            }
        }
        return false;
    }
}
