package com.ispilo.interceptor;

import com.ispilo.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // We can capture the start time here if we want to log duration in afterCompletion
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String method = request.getMethod();
        
        // Only audit state-changing operations (POST, PUT, DELETE) or specific GETs if needed
        if ("GET".equals(method) || "OPTIONS".equals(method)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Assuming the principal is the email or we can extract the user ID
            // In a real scenario, you might cast principal to your UserDetails implementation
             userId = authentication.getName(); // Or extract ID
        }

        String uri = request.getRequestURI();
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = request.getHeader("X-Device-Info"); // Custom header from mobile app

        Map<String, Object> details = new HashMap<>();
        details.put("method", method);
        details.put("status", response.getStatus());
        details.put("query", request.getQueryString());
        
        if (ex != null) {
            details.put("exception", ex.getMessage());
        }

        // Determine action based on method and URI
        String action = method + " " + uri;
        String resourceType = "API"; 
        String resourceId = null; // Could extract from URI path variables if needed

        // Call async audit service
        auditService.logAction(userId, action, resourceType, resourceId, details, ipAddress, userAgent, deviceInfo);
    }
}
