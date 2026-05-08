package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.AdminPromoteRequest;
import com.ispilo.model.dto.request.AdminReportReviewRequest;
import com.ispilo.model.dto.response.AdminDashboardStatsResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.entity.BannedDevice;
import com.ispilo.model.entity.AuditLog;
import com.ispilo.model.entity.ProductReport;
import com.ispilo.model.entity.SellerReport;
import com.ispilo.model.entity.User;
import com.ispilo.model.enums.AdminReportAction;
import com.ispilo.model.enums.ReportStatus;
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
    private final ProductReportRepository productReportRepository;
    private final SellerReportRepository sellerReportRepository;
    private final BannedDeviceRepository bannedDeviceRepository;
    private final BannedDeviceCacheService bannedDeviceCacheService;
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

    public ReportResponse reviewProductReport(String reportId, AdminReportReviewRequest request) {
        ProductReport report = productReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Product report not found"));

        applyReportDecision(request, report.getSeller(), report.getProduct() != null ? report.getProduct().getDeviceId() : null, report);
        productReportRepository.save(report);

        return ReportResponse.builder()
                .id(report.getId())
                .targetId(report.getProduct().getId())
                .targetType("PRODUCT")
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public ReportResponse reviewSellerReport(String reportId, AdminReportReviewRequest request) {
        SellerReport report = sellerReportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Seller report not found"));

        applyReportDecision(request, report.getSeller(), null, report);
        sellerReportRepository.save(report);

        return ReportResponse.builder()
                .id(report.getId())
                .targetId(report.getSeller().getId())
                .targetType("SELLER")
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private void applyReportDecision(AdminReportReviewRequest request, com.ispilo.model.entity.Seller seller, String deviceId, Object reportEntity) {
        if (request.getStatus() == null) {
            throw new NotFoundException("Report status is required");
        }

        if (request.getAction() == AdminReportAction.REJECT) {
            setReportStatus(reportEntity, ReportStatus.REVIEWED);
            return;
        }

        if (request.getAction() == AdminReportAction.FLAG_SELLER) {
            blockSellerUploads(seller, request);
        }

        if (request.getAction() == AdminReportAction.BAN_DEVICE) {
            if (deviceId == null || deviceId.isBlank()) {
                throw new NotFoundException("Device ID not found for report");
            }
            if (!bannedDeviceRepository.existsByDeviceId(deviceId)) {
                bannedDeviceRepository.save(BannedDevice.builder()
                        .deviceId(deviceId)
                        .reason("REPORT_ACTION")
                        .note(request.getNote())
                        .build());
                bannedDeviceCacheService.refreshCache();
            }
        }

        setReportStatus(reportEntity, request.getStatus());
    }

    private void blockSellerUploads(com.ispilo.model.entity.Seller seller, AdminReportReviewRequest request) {
        if (seller == null) {
            throw new NotFoundException("Seller not found for report action");
        }
        int hours = request.getBlockHours() != null ? request.getBlockHours() : 168;
        seller.setIsFlagged(true);
        seller.setUploadBlockedUntil(java.time.LocalDateTime.now().plusHours(hours));
        seller.setUploadBlockReason(request.getNote() != null ? request.getNote() : "Admin report action");
        sellerRepository.save(seller);
    }

    private void setReportStatus(Object reportEntity, ReportStatus status) {
        if (reportEntity instanceof ProductReport productReport) {
            productReport.setStatus(status);
        } else if (reportEntity instanceof SellerReport sellerReport) {
            sellerReport.setStatus(status);
        }
    }
}
