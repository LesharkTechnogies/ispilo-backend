package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.UnauthorizedException;
import com.ispilo.exception.ConflictException;
import com.ispilo.exception.BadRequestException;
import com.ispilo.model.dto.request.AdminFlagRequest;
import com.ispilo.model.dto.request.AdminProductUpdateRequest;
import com.ispilo.model.dto.request.AdminSellerUpdateRequest;
import com.ispilo.model.dto.request.AdminUserCreateRequest;
import com.ispilo.model.dto.request.AdminUserUpdateRequest;
import com.ispilo.model.dto.request.AdminPromoteRequest;
import com.ispilo.model.dto.request.AdminReportReviewRequest;
import com.ispilo.model.dto.response.AdminDashboardStatsResponse;
import com.ispilo.model.dto.response.MessageExportResponse;
import com.ispilo.model.dto.response.MessageResponse;
import com.ispilo.model.dto.response.ProductResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.dto.response.SellerResponse;
import com.ispilo.model.dto.response.UserResponse;
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
import com.ispilo.model.enums.VerificationStatus;
import com.ispilo.repository.*;
import com.ispilo.security.SecurityEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final SmsAuditRepository smsAuditRepository;

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

    public Page<com.ispilo.model.entity.SmsAudit> getSmsAudits(String phone, String status, @NonNull Pageable pageable) {
        if (phone != null && !phone.isBlank()) {
            return smsAuditRepository.findByPhone(phone, pageable);
        } else if (status != null && !status.isBlank()) {
            return smsAuditRepository.findByStatus(status, pageable);
        }
        return smsAuditRepository.findAll(pageable);
    }

    public Page<UserResponse> getUsers(String query, Pageable pageable) {
        Page<User> page = (query != null && !query.isBlank())
                ? userRepository.searchUsers(query, pageable)
                : userRepository.findAll(pageable);
        return page.map(UserResponse::fromEntity);
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public UserResponse createUser(AdminUserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone is already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .name(request.getFirstName() + " " + request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .countryCode(request.getCountryCode())
                .county(request.getCounty())
                .town(request.getTown())
                .isAdmin(Boolean.TRUE.equals(request.getIsAdmin()))
                .isVerified(Boolean.TRUE.equals(request.getIsVerified()))
                .build();

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public UserResponse updateUser(String userId, AdminUserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new ConflictException("Phone is already in use");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        } else if (request.getFirstName() != null || request.getLastName() != null) {
            String firstName = request.getFirstName() != null ? request.getFirstName() : user.getFirstName();
            String lastName = request.getLastName() != null ? request.getLastName() : user.getLastName();
            if (firstName != null && lastName != null) {
                user.setName(firstName + " " + lastName);
            }
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getIsAdmin() != null) {
            user.setIsAdmin(request.getIsAdmin());
        }
        if (request.getIsVerified() != null) {
            user.setIsVerified(request.getIsVerified());
        }
        if (request.getIsEmailVerified() != null) {
            user.setIsEmailVerified(request.getIsEmailVerified());
        }
        if (request.getIsPhoneVerified() != null) {
            user.setIsPhoneVerified(request.getIsPhoneVerified());
        }
        if (request.getProfilePublic() != null) {
            user.setProfilePublic(request.getProfilePublic());
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public UserResponse flagUser(String userId, AdminFlagRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean flagged = request.getFlagged() == null || Boolean.TRUE.equals(request.getFlagged());
        user.setIsFlagged(flagged);
        if (flagged) {
            user.setFlagReason(request.getReason());
            if (request.getBlockHours() != null && request.getBlockHours() > 0) {
                user.setBlockedUntil(LocalDateTime.now().plusHours(request.getBlockHours()));
            }
        } else {
            user.setFlagReason(null);
            user.setBlockedUntil(null);
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public Page<SellerResponse> getSellers(String query, Pageable pageable) {
        Page<com.ispilo.model.entity.Seller> page = (query != null && !query.isBlank())
                ? sellerRepository.findByBusinessNameContainingIgnoreCase(query, pageable)
                : sellerRepository.findAll(pageable);
        return page.map(SellerResponse::fromEntity);
    }

    public SellerResponse getSellerById(String sellerId) {
        com.ispilo.model.entity.Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));
        return SellerResponse.fromEntity(seller);
    }

    public SellerResponse updateSeller(String sellerId, AdminSellerUpdateRequest request) {
        com.ispilo.model.entity.Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        if (request.getBusinessName() != null) {
            seller.setBusinessName(request.getBusinessName());
        }
        if (request.getBusinessDescription() != null) {
            seller.setBusinessDescription(request.getBusinessDescription());
        }
        if (request.getBusinessAddress() != null) {
            seller.setBusinessAddress(request.getBusinessAddress());
        }
        if (request.getVerificationLevel() != null) {
            seller.setVerificationLevel(request.getVerificationLevel());
        }
        if (request.getIsVerified() != null) {
            seller.setIsVerified(request.getIsVerified());
            if (Boolean.TRUE.equals(request.getIsVerified())) {
                seller.setVerificationStatus(VerificationStatus.APPROVED);
            } else {
                seller.setVerificationStatus(VerificationStatus.REJECTED);
            }
        }

        return SellerResponse.fromEntity(sellerRepository.save(seller));
    }

    public SellerResponse flagSeller(String sellerId, AdminFlagRequest request) {
        com.ispilo.model.entity.Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        boolean flagged = request.getFlagged() == null || Boolean.TRUE.equals(request.getFlagged());
        seller.setIsFlagged(flagged);
        if (flagged) {
            if (request.getBlockHours() != null && request.getBlockHours() > 0) {
                seller.setUploadBlockedUntil(LocalDateTime.now().plusHours(request.getBlockHours()));
            }
            seller.setUploadBlockReason(request.getReason());
        } else {
            seller.setUploadBlockedUntil(null);
            seller.setUploadBlockReason(null);
        }

        return SellerResponse.fromEntity(sellerRepository.save(seller));
    }

    public Page<ProductResponse> getProducts(String query, Pageable pageable) {
        Page<com.ispilo.model.entity.Product> page = (query != null && !query.isBlank())
                ? productRepository.searchProducts(query, pageable)
                : productRepository.findAll(pageable);
        return page.map(ProductResponse::fromEntity);
    }

    public ProductResponse getProductByIdAdmin(String productId) {
        com.ispilo.model.entity.Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return ProductResponse.fromEntity(product);
    }

    public ProductResponse updateProductAdmin(String productId, AdminProductUpdateRequest request) {
        com.ispilo.model.entity.Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (request.getName() != null) {
            product.setTitle(request.getName());
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            if (request.getPrice().doubleValue() <= 0) {
                throw new BadRequestException("Product price must be greater than 0");
            }
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStockQuantity(request.getStock());
        }
        if (request.getIsAvailable() != null) {
            product.setIsAvailable(request.getIsAvailable());
        }
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            product.setImages(request.getImageUrls());
            product.setMainImage(request.getImageUrls().get(0));
        }

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    public ProductResponse flagProduct(String productId, AdminFlagRequest request) {
        com.ispilo.model.entity.Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        boolean flagged = request.getFlagged() == null || Boolean.TRUE.equals(request.getFlagged());
        product.setIsFlagged(flagged);
        if (flagged) {
            product.setFlagReason(request.getReason());
            if (request.getBlockHours() != null && request.getBlockHours() > 0) {
                product.setBlockedUntil(LocalDateTime.now().plusHours(request.getBlockHours()));
            }
            product.setIsAvailable(false);
        } else {
            product.setFlagReason(null);
            product.setBlockedUntil(null);
        }

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    public Page<MessageResponse> getMessages(String conversationId, String senderId, Pageable pageable) {
        Page<Message> page;
        if (conversationId != null && !conversationId.isBlank()) {
            page = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
        } else if (senderId != null && !senderId.isBlank()) {
            page = messageRepository.findBySenderId(senderId, pageable);
        } else {
            page = messageRepository.findAll(pageable);
        }

        return page.map(this::buildAdminMessageResponse);
    }

    public MessageResponse getMessageById(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        return buildAdminMessageResponse(message);
    }

    public void deleteMessageForEveryoneAdmin(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        message.setDeletedForEveryone(true);
        messageRepository.save(message);
    }

    public Page<AuditLog> getAuditTrace(String userId, String action, String resourceType, String resourceId,
                                        LocalDateTime fromTime, LocalDateTime toTime, Pageable pageable) {
        return auditLogRepository.findAuditTrace(userId, action, resourceType, resourceId, fromTime, toTime, pageable);
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

    private MessageResponse buildAdminMessageResponse(Message message) {
        String content = message.getContent();
        Conversation conversation = message.getConversation();
        if (Boolean.TRUE.equals(message.getDeletedForEveryone())) {
            content = "[deleted]";
        } else if (content != null && conversation.getEncryptionKey() != null) {
            try {
                content = encryptionService.decryptWithAES(content, conversation.getEncryptionKey());
            } catch (Exception e) {
                content = "[Encrypted message]";
            }
        }

        MessageResponse response = MessageResponse.fromEntity(message);
        response.setContent(content);
        response.setDeletedForEveryone(Boolean.TRUE.equals(message.getDeletedForEveryone()));
        response.setReadByCount(messageReadRepository.countByMessageId(message.getId()));
        if (Boolean.TRUE.equals(message.getDeletedForEveryone())) {
            response.setMediaUrl(null);
        }
        return response;
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
