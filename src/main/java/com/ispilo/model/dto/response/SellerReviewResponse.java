package com.ispilo.model.dto.response;

import com.ispilo.model.entity.SellerReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerReviewResponse {

    private String id;
    private String sellerId;
    private UserResponse user;
    private Integer rating;
    private String comment;
    private String title;
    private Boolean wouldRecommend;
    private LocalDateTime createdAt;

    public static SellerReviewResponse fromEntity(SellerReview review) {
        return SellerReviewResponse.builder()
                .id(review.getId())
                .sellerId(review.getSeller().getId())
                .user(UserResponse.fromEntity(review.getUser()))
                .rating(review.getRating())
                .comment(review.getComment())
                .title(review.getTitle())
                .wouldRecommend(review.getWouldRecommend())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
