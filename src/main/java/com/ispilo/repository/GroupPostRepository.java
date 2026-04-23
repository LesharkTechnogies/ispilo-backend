package com.ispilo.repository;

import com.ispilo.model.entity.GroupEntity;
import com.ispilo.model.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupPostRepository extends JpaRepository<PostEntity, String> {
    Page<PostEntity> findByGroup(GroupEntity group, Pageable p);
}
