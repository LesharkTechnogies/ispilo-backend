package com.ispilo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupEntity {
    @Id
    private String id;

    private String name;
    private String description;
    private boolean isPrivate;

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    @OneToMany(mappedBy = "group")
    private Set<GroupMembership> memberships;
}
