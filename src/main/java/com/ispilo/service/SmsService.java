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
        SmsAudit audit = SmsAudit.builder()
                .phone(recipient)
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
            body.put("recipient", recipient);
            body.put("sender_id", senderId);
            body.put("type", "plain");
            body.put("message", message);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(TALKSASA_URL, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map responseBody = response.getBody();
                if ("success".equals(responseBody.get("status"))) {
                    log.info("SMS sent successfully to: {}", recipient);
                    audit.setStatus("DELIVERED");
                } else {
                    log.error("Failed to send SMS to {}. TalkSasa Response: {}", recipient, responseBody);
                    audit.setStatus("UNSENT");
                }
            } else {
                log.error("Failed to send SMS to {}. HTTP Status: {}", recipient, response.getStatusCode());
                audit.setStatus("UNSENT");
            }
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", recipient, e.getMessage(), e);
            audit.setStatus("UNSENT");
        } finally {
            smsAuditRepository.save(audit);
        }
    }
}

