package com.ispilo.repository;

import com.ispilo.model.entity.SmsAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsAuditRepository extends JpaRepository<SmsAudit, String> {
    Page<SmsAudit> findByPhone(String phone, Pageable pageable);
    Page<SmsAudit> findByStatus(String status, Pageable pageable);
}
