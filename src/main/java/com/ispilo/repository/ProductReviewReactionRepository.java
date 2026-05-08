package com.ispilo.repository;

import com.ispilo.model.entity.ProductReview;
import com.ispilo.model.entity.ProductReviewReaction;
import com.ispilo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductReviewReactionRepository extends JpaRepository<ProductReviewReaction, String> {
    Optional<ProductReviewReaction> findByReviewAndUser(ProductReview review, User user);
}
