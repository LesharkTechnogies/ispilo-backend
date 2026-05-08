package com.ispilo.repository;

import com.ispilo.model.entity.Seller;
import com.ispilo.model.entity.SellerReview;
import com.ispilo.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerReviewRepository extends JpaRepository<SellerReview, String> {

    Page<SellerReview> findBySellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);

    Optional<SellerReview> findBySellerAndUser(Seller seller, User user);

    boolean existsBySellerAndUser(Seller seller, User user);

    @Query("SELECT AVG(r.rating) FROM SellerReview r WHERE r.seller.id = :sellerId")
    Double calculateAverageRating(@Param("sellerId") String sellerId);
}
