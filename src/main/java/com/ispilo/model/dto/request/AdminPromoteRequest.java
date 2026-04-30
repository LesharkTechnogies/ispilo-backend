package com.ispilo.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminPromoteRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    /**
     * Optional target user email to promote. If omitted, the authenticated admin email is promoted.
     */
    @Email
    private String targetEmail;
}
