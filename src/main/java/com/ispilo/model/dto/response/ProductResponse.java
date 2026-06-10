package com.ispilo.model.dto.response;

import com.ispilo.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String id;
    private String sellerId;
    private String sellerName;
    private String sellerLogo;
    private String sellerVerificationLevel;
    private Double sellerRating;
    private String title;
    private String name; // Kept for backward compatibility
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String mainImage;
    private List<String> images;
    private String category;
    private String condition;
    private String location;
    private Double rating;
    private Integer reviewCount;
    private Boolean isAvailable;
    private Boolean isFeatured;
    private Boolean isFlagged;
    private String flagReason;
    private LocalDateTime blockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse fromEntity(Product product) {
    String sellerId = null;
    String sellerName = null;
    String sellerLogo = null;
    String sellerVerificationLevel = null;
    Double sellerRating = null;

    if (product.getSeller() != null) {
        sellerId = product.getSeller().getId();
        sellerName = product.getSeller().getBusinessName();
        sellerLogo = product.getSeller().getBusinessLogo();
        sellerVerificationLevel = product.getSeller().getVerificationLevel() != null
            ? product.getSeller().getVerificationLevel().name()
            : null;
        sellerRating = product.getSeller().getRating();
    }

        return ProductResponse.builder()
                .id(product.getId())
        .sellerId(sellerId)
        .sellerName(sellerName)
        .sellerLogo(sellerLogo)
        .sellerVerificationLevel(sellerVerificationLevel)
        .sellerRating(sellerRating)
                .title(product.getTitle())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .mainImage(product.getMainImage())
                .images(product.getImages())
                .category(product.getCategory())
                .condition(product.getCondition())
                .location(product.getLocation())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .isAvailable(product.getIsAvailable())
                .isFeatured(product.getIsFeatured())
                .isFlagged(product.getIsFlagged())
                .flagReason(product.getFlagReason())
                .blockedUntil(product.getBlockedUntil())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
