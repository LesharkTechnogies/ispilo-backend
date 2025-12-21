package com.ispilo.repository;

import com.ispilo.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Page<Product> findBySellerId(String sellerId, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByIsAvailable(Boolean isAvailable, Pageable pageable);
}

