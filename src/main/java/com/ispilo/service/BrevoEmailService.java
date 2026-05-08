package com.ispilo.service;

import com.ispilo.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrevoEmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api-key:${smtp_api_key1:}}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:}")
    private String senderEmail;

    @Value("${brevo.sender-name:Ispilo}")
    private String senderName;

    @org.springframework.scheduling.annotation.Async
    public void sendForgotPasswordCode(String recipientEmail, String recipientName, String code, long ttlMinutes) {
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            throw new BadRequestException("Email service is not configured");
        }

        if (senderEmail == null || senderEmail.isBlank()) {
            throw new BadRequestException("Sender email is not configured");
        }

        String safeName = (recipientName == null || recipientName.isBlank()) ? "there" : recipientName;
        String htmlContent = buildForgotPasswordHtml(safeName, code, ttlMinutes);

        Map<String, Object> payload = Map.of(
                "htmlContent", htmlContent,
                "sender", Map.of(
                        "email", senderEmail,
                        "name", senderName
                ),
                "subject", "Ispilo Password Reset Code",
                "to", List.of(Map.of(
                        "email", recipientEmail,
                        "name", safeName
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
        ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
            new HttpEntity<>(payload, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Brevo send failed with status {} and body {}", response.getStatusCode(), response.getBody());
                throw new BadRequestException("Failed to send verification email");
            }
        } catch (Exception ex) {
            log.error("Brevo send failed: {}", ex.getMessage());
            throw new BadRequestException("Failed to send verification email");
        }
    }

    private String buildForgotPasswordHtml(String name, String code, long ttlMinutes) {
        return """
                <html>
                  <body style=\"font-family: Arial, sans-serif; background-color: #f3fbf4; padding: 24px;\">
                    <div style=\"max-width: 560px; margin: auto; background: #ffffff; border: 1px solid #d9f3de; border-radius: 12px; padding: 24px;\">
                      <h2 style=\"margin-top: 0; color: #1b7f3a;\">Ispilo Password Reset</h2>
                      <p style=\"color: #0f172a;\">Hi %s,</p>
                      <p style=\"color: #0f172a;\">Use the verification code below to reset your password:</p>
                      <p style=\"font-size: 30px; font-weight: 700; letter-spacing: 4px; color: #1b7f3a; margin: 18px 0;\">%s</p>
                      <p style=\"color: #334155;\">This code expires in <strong>%d minutes</strong>.</p>
                      <p style=\"color: #64748b;\">If you did not request this, you can ignore this email.</p>
                    </div>
                  </body>
                </html>
                """.formatted(name, code, ttlMinutes);
    }
}
