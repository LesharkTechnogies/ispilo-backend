package com.ispilo.repository;

import com.ispilo.model.entity.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReportRepository extends JpaRepository<ProductReport, String> {
}
