package com.ispilo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ispilo.model.entity.AuditLog;
import com.ispilo.model.entity.User;
import com.ispilo.repository.AuditLogRepository;
import com.ispilo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void logAction(String userId, String action, String resourceType, String resourceId, Map<String, Object> details) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }

            String ipAddress = "Unknown";
            String userAgent = "Unknown";
            String deviceInfo = "Unknown";

            // Try to get request details if available in the current context
            // Note: In @Async, RequestContext might not be available unless configured, 
            // but we'll try to capture it or pass it if needed. 
            // For simplicity in this implementation, we assume basic context or passed values.
            // A more robust way is to pass these as arguments to the async method.
            
            // Construct the JSON details
            String detailsJson = "{}";
            if (details != null) {
                detailsJson = objectMapper.writeValueAsString(details);
            }

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(user != null ? user.getEmail() : null)
                    .userPhone(user != null ? user.getPhone() : null)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(detailsJson)
                    .ipAddress(ipAddress) // In a real app, pass this from the controller
                    .userAgent(userAgent) // In a real app, pass this from the controller
                    .deviceInfo(deviceInfo) // This would come from a custom header like "X-Device-Info"
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log saved: User {} performed {} on {} {}", userId, action, resourceType, resourceId);

        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
    
    // Overloaded method to include request metadata explicitly
    @Async
    public void logAction(String userId, String action, String resourceType, String resourceId, Map<String, Object> details, String ipAddress, String userAgent, String deviceInfo) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }

            String detailsJson = "{}";
            if (details != null) {
                detailsJson = objectMapper.writeValueAsString(details);
            }

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(user != null ? user.getEmail() : null)
                    .userPhone(user != null ? user.getPhone() : null)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(detailsJson)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .deviceInfo(deviceInfo)
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}
