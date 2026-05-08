package com.ispilo.model.dto.response;

import com.ispilo.model.entity.ProductReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewResponse {

    private String id;
    private String productId;
    private UserResponse user;
    private Integer rating;
    private String comment;
    private String title;
    private Boolean wouldRecommend;
    private Integer likeCount;
    private Integer dislikeCount;
    private Boolean isFlagged;
    private String flagReason;
    private LocalDateTime createdAt;

    public static ProductReviewResponse fromEntity(ProductReview review) {
        return ProductReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .user(UserResponse.fromEntity(review.getUser()))
                .rating(review.getRating())
                .comment(review.getComment())
                .title(review.getTitle())
                .wouldRecommend(review.getWouldRecommend())
                .likeCount(review.getLikeCount())
                .dislikeCount(review.getDislikeCount())
                .isFlagged(review.getIsFlagged())
                .flagReason(review.getFlagReason())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
