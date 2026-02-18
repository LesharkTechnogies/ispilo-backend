package com.ispilo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sellers", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_business_name", columnList = "business_name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Seller {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_description", length = 1000)
    private String businessDescription;

    @Column(name = "business_logo")
    private String businessLogo;

    @Column(name = "business_address")
    private String businessAddress;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verification_document")
    private String verificationDocument;

    @Column(name = "rating", columnDefinition = "DECIMAL(3,1) DEFAULT 4.5")
    @Builder.Default
    private Double rating = 4.5;

    @Column(name = "total_sales")
    @Builder.Default
    private Integer totalSales = 0;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for backward compatibility
    public String getName() {
        return businessName;
    }

    public String getAvatar() {
        return businessLogo;
    }
    
    public String getPhone() {
        return user != null ? user.getPhone() : null;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public Integer getTotalSales() {
        return totalSales;
    }
}
