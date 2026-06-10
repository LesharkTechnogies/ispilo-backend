package com.ispilo.repository;

import com.ispilo.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
    
    Page<AuditLog> findByAction(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE (:userId IS NULL OR a.userId = :userId) "
            + "AND (:action IS NULL OR a.action = :action) "
            + "AND (:resourceType IS NULL OR a.resourceType = :resourceType) "
            + "AND (:resourceId IS NULL OR a.resourceId = :resourceId) "
            + "AND (:fromTime IS NULL OR a.createdAt >= :fromTime) "
            + "AND (:toTime IS NULL OR a.createdAt <= :toTime)")
    Page<AuditLog> findAuditTrace(@Param("userId") String userId,
                                 @Param("action") String action,
                                 @Param("resourceType") String resourceType,
                                 @Param("resourceId") String resourceId,
                                 @Param("fromTime") LocalDateTime fromTime,
                                 @Param("toTime") LocalDateTime toTime,
                                 Pageable pageable);
}
