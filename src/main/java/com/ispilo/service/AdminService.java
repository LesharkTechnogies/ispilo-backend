package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.AdminPromoteRequest;
import com.ispilo.model.dto.response.AdminDashboardStatsResponse;
import com.ispilo.model.entity.AuditLog;
import com.ispilo.model.entity.User;
import com.ispilo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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

    public User promoteAdmin(AdminPromoteRequest request) {
        User adminUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Admin user not found"));

        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            throw new UnauthorizedException("Invalid admin credentials");
        }

        if (!Boolean.TRUE.equals(adminUser.getIsAdmin())) {
            throw new UnauthorizedException("Access denied. Admin privileges required.");
        }

        String targetEmail = request.getTargetEmail() != null && !request.getTargetEmail().isBlank()
                ? request.getTargetEmail()
                : adminUser.getEmail();

        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new NotFoundException("Target user not found"));

        if (!Boolean.TRUE.equals(targetUser.getIsAdmin())) {
            targetUser.setIsAdmin(true);
            userRepository.save(targetUser);
        }

        return targetUser;
    }
}
