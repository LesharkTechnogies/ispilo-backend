package com.ispilo.service;

import com.ispilo.exception.ConflictException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.BadRequestException;
import com.ispilo.model.dto.request.CreateSellerRequest;
import com.ispilo.model.dto.request.CreateReportRequest;
import com.ispilo.model.dto.request.SellerReviewRequest;
import com.ispilo.model.dto.request.SellerVerificationRequest;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.dto.response.SellerReviewResponse;
import com.ispilo.model.dto.response.SellerResponse;
import com.ispilo.model.entity.Seller;
import com.ispilo.model.entity.SellerReport;
import com.ispilo.model.entity.SellerReview;
import com.ispilo.model.entity.User;
import com.ispilo.repository.SellerRepository;
import com.ispilo.repository.SellerReportRepository;
import com.ispilo.repository.SellerReviewRepository;
import com.ispilo.repository.UserRepository;
import com.ispilo.model.enums.SellerVerificationLevel;
import com.ispilo.model.enums.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerReportRepository sellerReportRepository;
    private final SellerReviewRepository sellerReviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public SellerResponse createSeller(String userEmail, CreateSellerRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (sellerRepository.existsByUserId(user.getId())) {
            throw new ConflictException("User is already a seller");
        }

        Seller seller = Seller.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessDescription(request.getBusinessDescription())
                .businessAddress(request.getBusinessAddress())
                .verificationLevel(SellerVerificationLevel.UNVERIFIED)
                .verificationStatus(VerificationStatus.NONE)
                .isVerified(false)
                .build();

        seller = sellerRepository.save(seller);
        return SellerResponse.fromEntity(seller);
    }

    @Transactional
    public SellerResponse updateSellerDetails(String userEmail, CreateSellerRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Seller profile not found"));

        if (seller.getVerificationLevel() != SellerVerificationLevel.UNVERIFIED || seller.getVerificationStatus() == VerificationStatus.APPROVED) {
            throw new BadRequestException("Verified sellers cannot update their business details to prevent counterfeiting.");
        }

        seller.setBusinessName(request.getBusinessName());
        seller.setBusinessDescription(request.getBusinessDescription());
        seller.setBusinessAddress(request.getBusinessAddress());

        return SellerResponse.fromEntity(sellerRepository.save(seller));
    }

    @Transactional
    public void submitForIdVerification(String userEmail, String nationalIdImage, String phone, String fullName, String kraPin) {
    submitVerificationRequest(userEmail, SellerVerificationRequest.builder()
        .nationalIdImage(nationalIdImage)
        .phone(phone)
        .fullName(fullName)
                .kraPin(kraPin)
        .requestedLevel(SellerVerificationLevel.ID_VERIFIED)
        .build());
    }

    @Transactional
    public void submitVerificationRequest(String userEmail, SellerVerificationRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Seller profile not found"));

        if (seller.getVerificationLevel() == SellerVerificationLevel.FULLY_VERIFIED || seller.getVerificationLevel() == SellerVerificationLevel.ID_VERIFIED) {
            throw new BadRequestException("Seller is already verified.");
        }

        if (request.getRequestedLevel() == SellerVerificationLevel.UNVERIFIED) {
            throw new BadRequestException("Invalid verification level request.");
        }

        if (request.getKraPin() == null || request.getKraPin().trim().isEmpty()) {
            throw new BadRequestException("KRA PIN is required for ID Verified and Fully Verified sellers.");
        }

        if (!user.getPhone().equals(request.getPhone())) {
            throw new BadRequestException("The phone number provided for verification must match the registered user's phone number.");
        }

        seller.setNationalIdImage(request.getNationalIdImage());
        seller.setVerifiedPhone(request.getPhone());
        seller.setVerifiedFullName(request.getFullName());
        seller.setBusinessPin(request.getKraPin());
        seller.setVerificationStatus(VerificationStatus.PENDING);
        seller.setRequestedVerificationLevel(request.getRequestedLevel());

        sellerRepository.save(seller);
        log.info("Seller {} submitted verification request for {}. Admin notification triggered.", seller.getId(), request.getRequestedLevel());
        // TODO: Send notification to admin
    }

    @Transactional
    public SellerReviewResponse addSellerReview(String username, String sellerId, SellerReviewRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        if (seller.getUser() != null && seller.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Sellers cannot review themselves");
        }

        if (sellerReviewRepository.existsBySellerAndUser(seller, user)) {
            throw new BadRequestException("You have already reviewed this seller");
        }

        SellerReview review = SellerReview.builder()
                .seller(seller)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .title(request.getTitle())
                .wouldRecommend(request.getWouldRecommend())
                .build();

        review = sellerReviewRepository.save(review);
        updateSellerRating(seller);

        return SellerReviewResponse.fromEntity(review);
    }

    public PageResponse<?> getSellerReviews(String sellerId, org.springframework.data.domain.Pageable pageable) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new NotFoundException("Seller not found");
        }

        org.springframework.data.domain.Page<SellerReview> page = sellerReviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        return PageResponse.builder()
                .content(page.getContent().stream()
                        .map(SellerReviewResponse::fromEntity)
                        .collect(java.util.stream.Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private void updateSellerRating(Seller seller) {
        Double averageRating = sellerReviewRepository.calculateAverageRating(seller.getId());
        double rating = averageRating != null ? averageRating : 4.5;
        seller.setRating(Math.round(rating * 10.0) / 10.0);
        sellerRepository.save(seller);
    }

    @Transactional
    public ReportResponse reportSeller(String username, String sellerId, CreateReportRequest request) {
    User user = userRepository.findByEmail(username)
        .orElseGet(() -> userRepository.findByPhone(username)
            .orElseThrow(() -> new NotFoundException("User not found")));

    Seller seller = sellerRepository.findById(sellerId)
        .orElseThrow(() -> new NotFoundException("Seller not found"));

    SellerReport report = SellerReport.builder()
        .seller(seller)
        .reporter(user)
        .reason(request.getReason())
        .description(request.getDescription())
        .build();

    report = sellerReportRepository.save(report);

    return ReportResponse.builder()
        .id(report.getId())
        .targetId(seller.getId())
        .targetType("SELLER")
        .reason(report.getReason())
        .description(report.getDescription())
        .status(report.getStatus())
        .createdAt(report.getCreatedAt())
        .build();
    }
}
