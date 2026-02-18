package com.ispilo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_seller_id", columnList = "seller_id"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_rating", columnList = "rating")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String name; // Alias for title for backward compatibility

    @Column(length = 2000)
    private String description;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    private String mainImage; // Primary image URL

    private String category;

    @Column(name = "condition_type")
    private String condition; // New, Used, Refurbished

    @Column(name = "rating", columnDefinition = "DECIMAL(3,1) DEFAULT 4.5")
    @Builder.Default
    private Double rating = 4.5;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    private String location;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
