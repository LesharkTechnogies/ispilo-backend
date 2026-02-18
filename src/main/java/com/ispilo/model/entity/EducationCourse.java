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
@Table(name = "education_courses", indexes = {
    @Index(name = "idx_instructor", columnList = "instructor"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EducationCourse {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String instructor;

    @Column(name = "thumbnail_url")
    private String thumbnail;

    @Column(length = 2000)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "enrollment_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer enrollmentCount = 0;

    @Column(name = "rating", columnDefinition = "DECIMAL(3,1) DEFAULT 4.5")
    @Builder.Default
    private Double rating = 4.5;

    @Column(name = "duration_hours", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer durationHours = 0;

    @Column(name = "total_lessons", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer totalLessons = 0;

    @ElementCollection
    @CollectionTable(name = "course_topics", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "topic")
    @Builder.Default
    private List<String> topics = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
