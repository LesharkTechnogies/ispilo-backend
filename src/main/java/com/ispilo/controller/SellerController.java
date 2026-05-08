package com.ispilo.controller;

import com.ispilo.model.dto.request.CreateSellerRequest;
import com.ispilo.model.dto.request.CreateReportRequest;
import com.ispilo.model.dto.request.SellerReviewRequest;
import com.ispilo.model.dto.request.SellerVerificationRequest;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.dto.response.SellerReviewResponse;
import com.ispilo.model.dto.response.SellerResponse;
import com.ispilo.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/sellers", "/api/sellers", "/api/v2/sellers"})
@Tag(name = "Sellers", description = "APIs for seller accounts")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping
    @Operation(summary = "Create a seller profile for the current user")
    public ResponseEntity<SellerResponse> createSeller(
            @Valid @RequestBody CreateSellerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SellerResponse response = sellerService.createSeller(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verification")
    @Operation(summary = "Submit seller verification (ID verified or fully verified) with KRA PIN")
    public ResponseEntity<?> submitVerification(
            @Valid @RequestBody SellerVerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        sellerService.submitVerificationRequest(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Verification request submitted"));
    }

    @GetMapping("/{sellerId}/reviews")
    @Operation(summary = "Get seller reviews")
    public ResponseEntity<PageResponse<?>> getSellerReviews(
            @PathVariable String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(sellerService.getSellerReviews(sellerId, pageable));
    }

    @PostMapping("/{sellerId}/reviews")
    @Operation(summary = "Add seller review")
    public ResponseEntity<SellerReviewResponse> addSellerReview(
            @PathVariable String sellerId,
            @Valid @RequestBody SellerReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SellerReviewResponse response = sellerService.addSellerReview(userDetails.getUsername(), sellerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{sellerId}/reports")
    @Operation(summary = "Report a seller")
    public ResponseEntity<ReportResponse> reportSeller(
            @PathVariable String sellerId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sellerService.reportSeller(userDetails.getUsername(), sellerId, request));
    }
}
