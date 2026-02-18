package com.ispilo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "education_videos", indexes = {
    @Index(name = "idx_channel", columnList = "channel"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EducationVideo {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "thumbnail_url")
    private String thumbnail;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "duration")
    private String duration; // Format: "MM:SS"

    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer views = 0;

    @Column(length = 1000)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "rating", columnDefinition = "DECIMAL(3,1) DEFAULT 4.5")
    @Builder.Default
    private Double rating = 4.5;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
