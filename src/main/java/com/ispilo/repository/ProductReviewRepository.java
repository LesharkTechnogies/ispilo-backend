package com.ispilo.repository;

import com.ispilo.model.entity.ProductReview;
import com.ispilo.model.entity.Product;
import com.ispilo.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {

    Page<ProductReview> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

    Optional<ProductReview> findByProductAndUser(Product product, User user);

    boolean existsByProductAndUser(Product product, User user);

    long countByProductId(String productId);

    List<ProductReview> findTop5ByProductIdOrderByCreatedAtDesc(String productId);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
    Double calculateAverageRating(@Param("productId") String productId);

    @Query("SELECT COALESCE(SUM(r.likeCount), 0) FROM ProductReview r WHERE r.product.id = :productId")
    Long sumLikeCount(@Param("productId") String productId);

    @Query("SELECT COALESCE(SUM(r.dislikeCount), 0) FROM ProductReview r WHERE r.product.id = :productId")
    Long sumDislikeCount(@Param("productId") String productId);
}
