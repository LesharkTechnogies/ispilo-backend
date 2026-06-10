package com.ispilo.controller;

import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.AdminFlagRequest;
import com.ispilo.model.dto.request.AdminProductUpdateRequest;
import com.ispilo.model.dto.request.AdminPromoteRequest;
import com.ispilo.model.dto.request.AdminReportReviewRequest;
import com.ispilo.model.dto.request.AdminSellerUpdateRequest;
import com.ispilo.model.dto.request.AdminUserCreateRequest;
import com.ispilo.model.dto.request.AdminUserUpdateRequest;
import com.ispilo.model.dto.response.AdminDashboardStatsResponse;
import com.ispilo.model.dto.response.MessageExportResponse;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.model.dto.response.ProductResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.dto.response.SellerResponse;
import com.ispilo.model.dto.response.UserResponse;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
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

    @GetMapping("/sms-audits")
    public ResponseEntity<Page<com.ispilo.model.entity.SmsAudit>> getSmsAudits(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getSmsAudits(phone, status, pageable));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getUsers(query, pageable));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AdminUserCreateRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.updateUser(userId, request));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId) {
        verifyAdmin(userDetails.getUsername());
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/flag")
    public ResponseEntity<UserResponse> flagUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId,
            @RequestBody AdminFlagRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.flagUser(userId, request));
    }

    @GetMapping("/sellers")
    public ResponseEntity<Page<SellerResponse>> getSellers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getSellers(query, pageable));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<SellerResponse> getSellerById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sellerId) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.getSellerById(sellerId));
    }

    @PutMapping("/sellers/{sellerId}")
    public ResponseEntity<SellerResponse> updateSeller(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sellerId,
            @RequestBody AdminSellerUpdateRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.updateSeller(sellerId, request));
    }

    @PostMapping("/sellers/{sellerId}/flag")
    public ResponseEntity<SellerResponse> flagSeller(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sellerId,
            @RequestBody AdminFlagRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.flagSeller(sellerId, request));
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getProducts(query, pageable));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.getProductByIdAdmin(productId));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId,
            @RequestBody AdminProductUpdateRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.updateProductAdmin(productId, request));
    }

    @PostMapping("/products/{productId}/flag")
    public ResponseEntity<ProductResponse> flagProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String productId,
            @RequestBody AdminFlagRequest request) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.flagProduct(productId, request));
    }

    @GetMapping("/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String conversationId,
            @RequestParam(required = false) String senderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getMessages(conversationId, senderId, pageable));
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> getMessageById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String messageId) {
        verifyAdmin(userDetails.getUsername());
        return ResponseEntity.ok(adminService.getMessageById(messageId));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessageForEveryone(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String messageId) {
        verifyAdmin(userDetails.getUsername());
        adminService.deleteMessageForEveryoneAdmin(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-trace")
    public ResponseEntity<Page<AuditLog>> getAuditTrace(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifyAdmin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getAuditTrace(userId, action, resourceType, resourceId, from, to, pageable));
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

    @PostMapping("/messages/{messageId}/restore-everyone")
    public ResponseEntity<Void> restoreMessageForEveryone(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String messageId) {
        verifyAdmin(userDetails.getUsername());
        adminService.restoreMessageForEveryone(messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages/{messageId}/restore-for-user/{userId}")
    public ResponseEntity<Void> restoreMessageForUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String messageId,
            @PathVariable String userId) {
        verifyAdmin(userDetails.getUsername());
        adminService.restoreMessageForUser(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/{conversationId}/restore-for-user/{userId}")
    public ResponseEntity<Void> restoreConversationForUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId,
            @PathVariable String userId) {
        verifyAdmin(userDetails.getUsername());
        adminService.restoreConversationForUser(conversationId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations/{conversationId}/export")
    public ResponseEntity<java.util.List<MessageExportResponse>> exportConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId) {
        verifyAdmin(userDetails.getUsername());
        java.util.List<MessageExportResponse> payload = adminService.exportConversation(conversationId);
        String filename = "conversation-" + conversationId + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .body(payload);
    }
}
