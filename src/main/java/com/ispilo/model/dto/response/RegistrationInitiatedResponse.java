package com.ispilo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationInitiatedResponse {
    private String message;
    private String phone;
    private boolean requiresVerification;
}
