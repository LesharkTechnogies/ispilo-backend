package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.model.dto.request.AdminPromoteRequest;
import com.ispilo.model.dto.request.AdminReportReviewRequest;
import com.ispilo.model.dto.response.AdminDashboardStatsResponse;
import com.ispilo.model.dto.response.MessageExportResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.entity.BannedDevice;
import com.ispilo.model.entity.AuditLog;
import com.ispilo.model.entity.ProductReport;
import com.ispilo.model.entity.SellerReport;
import com.ispilo.model.entity.User;
import com.ispilo.model.entity.Message;
import com.ispilo.model.entity.MessageRead;
import com.ispilo.model.entity.Conversation;
import com.ispilo.model.enums.AdminReportAction;
import com.ispilo.model.enums.ReportStatus;
import com.ispilo.repository.*;
import com.ispilo.security.SecurityEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final ConversationRepository conversationRepository;
    private final SecurityEncryptionService encryptionService;
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

    public Page<AuditLog> getAuditLogs(@NonNull Pageable pageable) {
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
        String safeReportId = Objects.requireNonNull(reportId, "reportId");
        ProductReport report = productReportRepository.findById(safeReportId)
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
        String safeReportId = Objects.requireNonNull(reportId, "reportId");
        SellerReport report = sellerReportRepository.findById(safeReportId)
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

    public void restoreMessageForEveryone(String messageId) {
        String safeMessageId = Objects.requireNonNull(messageId, "messageId");
        Message message = messageRepository.findById(safeMessageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        message.setDeletedForEveryone(false);
        messageRepository.save(message);
    }

    public void restoreMessageForUser(String messageId, String userId) {
        String safeMessageId = Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(userId, "userId");
        Message message = messageRepository.findById(safeMessageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (message.getDeletedFor() != null) {
            message.getDeletedFor().remove(userId);
        }
        messageRepository.save(message);
    }

    public void restoreConversationForUser(String conversationId, String userId) {
        String safeConversationId = Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(userId, "userId");
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(safeConversationId);
        for (Message message : messages) {
            if (message.getDeletedFor() != null) {
                message.getDeletedFor().remove(userId);
            }
        }
        messageRepository.saveAll(Objects.requireNonNull(messages, "messages"));
    }

    public List<MessageExportResponse> exportConversation(String conversationId) {
        String safeConversationId = Objects.requireNonNull(conversationId, "conversationId");
        Conversation conversation = conversationRepository.findById(safeConversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(safeConversationId);
        if (messages.isEmpty()) {
            return java.util.List.of();
        }

        List<String> messageIds = messages.stream().map(Message::getId).toList();
        List<MessageRead> reads = messageReadRepository.findByMessageIdIn(messageIds);

        java.util.Map<String, List<MessageRead>> readsByMessage = reads.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> r.getMessage().getId()));

        java.util.Set<String> userIds = new java.util.HashSet<>();
        for (Message message : messages) {
            userIds.add(message.getSender().getId());
            if (message.getDeletedFor() != null) {
                userIds.addAll(message.getDeletedFor());
            }
        }
        for (MessageRead read : reads) {
            userIds.add(read.getUser().getId());
        }

        java.util.Map<String, String> userNames = userRepository.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, u -> u.getName() != null ? u.getName() : u.getEmail()));

        return messages.stream().map(message -> {
            List<String> readByUsers = readsByMessage.getOrDefault(message.getId(), java.util.List.of()).stream()
                    .map(read -> userNames.getOrDefault(read.getUser().getId(), read.getUser().getId()))
                    .toList();

            List<String> deletedForUsers = message.getDeletedFor() == null ? java.util.List.of() : message.getDeletedFor().stream()
                    .map(userId -> userNames.getOrDefault(userId, userId))
                    .toList();

            String content = message.getContent();
            if (Boolean.TRUE.equals(message.getDeletedForEveryone())) {
                content = "[deleted]";
            } else if (content != null && conversation.getEncryptionKey() != null) {
                try {
                    content = encryptionService.decryptWithAES(content, conversation.getEncryptionKey());
                } catch (Exception e) {
                    content = "[Encrypted message]";
                }
            }

            return MessageExportResponse.builder()
                    .id(message.getId())
                    .conversationId(conversation.getId())
                    .senderId(message.getSender().getId())
                    .senderName(userNames.getOrDefault(message.getSender().getId(), message.getSender().getId()))
                    .type(message.getType())
                    .content(content)
                    .mediaUrl(Boolean.TRUE.equals(message.getDeletedForEveryone()) ? null : message.getMediaUrl())
                    .deletedForEveryone(Boolean.TRUE.equals(message.getDeletedForEveryone()))
                    .deletedForUsers(deletedForUsers)
                    .readByUsers(readByUsers)
                    .createdAt(message.getCreatedAt())
                    .build();
        }).toList();
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
                BannedDevice bannedDevice = BannedDevice.builder()
                        .deviceId(deviceId)
                        .reason("REPORT_ACTION")
                        .note(request.getNote())
                        .build();
                bannedDeviceRepository.save(Objects.requireNonNull(bannedDevice));
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
