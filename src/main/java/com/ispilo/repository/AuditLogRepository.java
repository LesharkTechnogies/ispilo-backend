package com.ispilo.repository;

import com.ispilo.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
    
    Page<AuditLog> findByAction(String action, Pageable pageable);
}
