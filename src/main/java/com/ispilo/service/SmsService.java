package com.ispilo.service;

import com.ispilo.model.entity.SmsAudit;
import com.ispilo.repository.SmsAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    private final SmsAuditRepository smsAuditRepository;

    @Value("${talksasa.api-key}")
    private String apiKey;

    @Value("${talksasa.sender-id}")
    private String senderId;

    private static final String TALKSASA_URL = "https://bulksms.talksasa.com/api/v3/sms/send";
    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendSms(String recipient, String message) {
        String formattedRecipient = formatPhoneForTalkSasa(recipient);
        log.info("Sending SMS: Original={}, Formatted={}", recipient, formattedRecipient);

        SmsAudit audit = SmsAudit.builder()
                .phone(formattedRecipient)
                .message(message)
                .status("PENDING")
                .build();
        audit = smsAuditRepository.save(audit);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("Accept", "application/json");

            Map<String, String> body = new HashMap<>();
            body.put("recipient", formattedRecipient);
            body.put("type", "plain");
            body.put("sender_id", senderId);
            body.put("message", message);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(TALKSASA_URL, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map responseBody = response.getBody();
                if ("success".equals(responseBody.get("status"))) {
                    log.info("SMS request accepted successfully for: {}", formattedRecipient);
                    audit.setStatus("ACCEPTED");
                } else {
                    log.error("Failed to queue SMS for {}. TalkSasa Response: {}", formattedRecipient, responseBody);
                    audit.setStatus("UNSENT");
                }
            } else {
                log.error("Failed to queue SMS for {}. HTTP Status: {}", formattedRecipient, response.getStatusCode());
                audit.setStatus("UNSENT");
            }
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", formattedRecipient, e.getMessage(), e);
            audit.setStatus("UNSENT");
        } finally {
            smsAuditRepository.save(audit);
        }
    }

    private String formatPhoneForTalkSasa(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        
        // Remove any spaces, dashes, or parentheses
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Remove leading '+' if present
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }
        
        // Convert local Kenyan format (07... or 01...) to 254...
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            cleaned = "254" + cleaned.substring(1);
        }
        
        return cleaned;
    }
}
