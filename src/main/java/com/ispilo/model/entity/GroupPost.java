package com.ispilo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "group_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPost {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    private User author;

    @Lob
    private String text;

    @ElementCollection
    private Set<String> mediaUrls;

    private boolean anonymous;

    private Instant createdAt;

    @OneToMany(mappedBy = "post")
    private Set<GroupPostLike> likes;
}
