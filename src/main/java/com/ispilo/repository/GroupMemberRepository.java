package com.ispilo.repository;

import com.ispilo.model.entity.GroupEntity;
import com.ispilo.model.entity.GroupMembershipEntity;
import com.ispilo.model.enums.GroupRole;
import com.ispilo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMembershipEntity, String> {
    Optional<GroupMembershipEntity> findByGroupAndUser(GroupEntity group, User user);
    Optional<GroupMembershipEntity> findByGroupAndUserAndRole(GroupEntity group, User user, GroupRole role);
    long countByGroup(GroupEntity group);
    List<GroupMembershipEntity> findByGroup(GroupEntity group);
     boolean existsByGroupAndUser(GroupEntity group, User user);
    Page<GroupMembershipEntity> findByGroup(GroupEntity group, Pageable pageable);
    Page<GroupMembershipEntity> findByUser(User user, Pageable pageable);
    List<GroupMembershipEntity> findByGroupAndRole(GroupEntity group, GroupRole role);

    void deleteByGroupAndUser(GroupEntity group, User user);
    void deleteByGroup(GroupEntity group);
    void deleteByUser(User user);
    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END FROM GroupMembershipEntity gm " +
            " WHERE gm.group = :group AND gm.user = :user AND gm.role = :role")
    Boolean isUserWithRole(@Param("group") GroupEntity group, 
    @Param("user") User user, 
    @Param("role") GroupRole role);
    
}