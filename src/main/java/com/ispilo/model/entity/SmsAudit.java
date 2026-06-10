package com.ispilo.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_audits", indexes = {
        @Index(name = "idx_sms_audit_phone", columnList = "phone"),
        @Index(name = "idx_sms_audit_status", columnList = "status"),
        @Index(name = "idx_sms_audit_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String status; // DELIVERED, UNSENT, PENDING

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
