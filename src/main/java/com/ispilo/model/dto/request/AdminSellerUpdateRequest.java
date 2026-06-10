package com.ispilo.model.dto.request;

import com.ispilo.model.enums.SellerVerificationLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSellerUpdateRequest {
    private String businessName;
    private String businessDescription;
    private String businessAddress;
    private SellerVerificationLevel verificationLevel;
    private Boolean isVerified;
}
