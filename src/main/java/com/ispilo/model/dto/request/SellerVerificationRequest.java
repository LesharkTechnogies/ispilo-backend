package com.ispilo.model.dto.request;

import com.ispilo.model.enums.SellerVerificationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerVerificationRequest {

    @NotBlank(message = "National ID image is required")
    private String nationalIdImage;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "KRA PIN is required")
    private String kraPin;

    @NotNull(message = "Requested verification level is required")
    private SellerVerificationLevel requestedLevel;
}
