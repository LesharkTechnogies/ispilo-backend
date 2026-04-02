package com.ispilo.controller;

import com.ispilo.model.dto.request.CreateSellerRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
