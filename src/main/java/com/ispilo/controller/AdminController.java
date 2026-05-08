package com.ispilo.controller;

import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.AdminPromoteRequest;
import com.ispilo.model.dto.request.AdminReportReviewRequest;
import com.ispilo.model.dto.response.AdminDashboardStatsResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.entity.AuditLog;
import com.ispilo.model.entity.User;
import com.ispilo.repository.UserRepository;
import com.ispilo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    private void verifyAdmin(String username) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new UnauthorizedException("User not found")));
        if (!Boolean.TRUE.equals(user.getIsAdmin())) {
            throw new UnauthorizedException("Access denied. Admin privileges required.");
        }
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getAuditLogs(pageable));
    }

    @PostMapping("/promote")
    public ResponseEntity<Map<String, Object>> promoteAdmin(@Valid @RequestBody AdminPromoteRequest request) {
        User promotedUser = adminService.promoteAdmin(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User promoted to admin",
                "userId", promotedUser.getId(),
                "email", promotedUser.getEmail()
        ));
    }

    @PostMapping("/reports/products/{reportId}/review")
    public ResponseEntity<ReportResponse> reviewProductReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String reportId,
            @Valid @RequestBody AdminReportReviewRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.reviewProductReport(reportId, request));
    }

    @PostMapping("/reports/sellers/{reportId}/review")
    public ResponseEntity<ReportResponse> reviewSellerReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String reportId,
            @Valid @RequestBody AdminReportReviewRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.reviewSellerReport(reportId, request));
    }
}
