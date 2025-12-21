package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Seller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerResponse {

    private String id;
    private String userId;
    private String businessName;
    private String businessDescription;
    private String businessLogo;
    private String businessAddress;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SellerResponse fromEntity(Seller seller) {
        return SellerResponse.builder()
                .id(seller.getId())
                .userId(seller.getUser().getId())
                .businessName(seller.getBusinessName())
                .businessDescription(seller.getBusinessDescription())
                .businessLogo(seller.getBusinessLogo())
                .businessAddress(seller.getBusinessAddress())
                .isVerified(seller.getIsVerified())
                .createdAt(seller.getCreatedAt())
                .updatedAt(seller.getUpdatedAt())
                .build();
    }
}
