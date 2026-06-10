package com.ispilo.repository;

import com.ispilo.model.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {

    Optional<Seller> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Page<Seller> findByBusinessNameContainingIgnoreCase(String businessName, Pageable pageable);
}

