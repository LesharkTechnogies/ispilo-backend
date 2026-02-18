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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
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
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
