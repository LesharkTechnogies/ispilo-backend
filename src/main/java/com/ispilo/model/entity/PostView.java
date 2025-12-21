package com.ispilo.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_views", uniqueConstraints = {
    @UniqueConstraint(name = "unique_user_post_view", columnNames = {"user_id", "post_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "post_id", nullable = false)
    private String postId;

    @Column(name = "view_percentage")
    @Builder.Default
    private Double viewPercentage = 0.0;

    @Column(name = "view_duration_ms")
    @Builder.Default
    private Integer viewDurationMs = 0;

    @Column(name = "is_viewed_completely")
    @Builder.Default
    private Boolean isViewedCompletely = false;

    @CreationTimestamp
    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;
}
