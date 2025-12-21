package com.ispilo.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSellerRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String businessDescription;
    private String businessAddress;
}

