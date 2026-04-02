package com.ispilo.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSellerRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business description is required")
    @Length(min = 10, max = 1000, message = "Business description must be between 10 and 1000 characters")
    private String businessDescription;

    @NotBlank(message = "Business address is required")
    private String businessAddress;
}

