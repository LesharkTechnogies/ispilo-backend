package com.ispilo.service;

import com.ispilo.model.dto.response.AdminDashboardStatsResponse;
import com.ispilo.model.entity.AuditLog;
import com.ispilo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final GroupPostRepository groupPostRepository;
    private final GroupRepository groupRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminDashboardStatsResponse getDashboardStats() {
        return AdminDashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalPosts(postRepository.count())
                .totalGroupPosts(groupPostRepository.count())
                .totalGroups(groupRepository.count())
                .totalProducts(productRepository.count())
                .totalSellers(sellerRepository.count())
                .build();
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}
