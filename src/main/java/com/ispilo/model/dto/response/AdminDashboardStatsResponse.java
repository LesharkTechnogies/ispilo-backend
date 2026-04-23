package com.ispilo.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardStatsResponse {
    private long totalUsers;
    private long totalPosts;
    private long totalGroupPosts;
    private long totalGroups;
    private long totalProducts;
    private long totalSellers;
}
