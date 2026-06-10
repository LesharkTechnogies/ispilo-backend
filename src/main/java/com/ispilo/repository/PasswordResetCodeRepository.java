package com.ispilo.repository;

import com.ispilo.model.entity.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, String> {

    Optional<PasswordResetCode> findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(String phone);

    @Modifying
    @Query("UPDATE PasswordResetCode p SET p.used = true WHERE p.phone = :phone AND p.used = false")
    int markAllUnusedAsUsed(@Param("phone") String phone);
}
