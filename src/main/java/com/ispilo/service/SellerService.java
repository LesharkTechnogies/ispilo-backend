package com.ispilo.service;

import com.ispilo.exception.ConflictException;
import com.ispilo.exception.NotFoundException;
import com.ispilo.model.dto.request.CreateSellerRequest;
import com.ispilo.model.dto.response.SellerResponse;
import com.ispilo.model.entity.Seller;
import com.ispilo.model.entity.User;
import com.ispilo.repository.SellerRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
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
        .isVerified(true)
                .build();

        seller = sellerRepository.save(seller);

        // TODO: Add 'ROLE_SELLER' to the user's roles.
        // This part is not implemented yet as roles are not stored in the database.
        // Once roles are implemented, the following line should be added:
        // user.getRoles().add("ROLE_SELLER");
        // userRepository.save(user);

        return SellerResponse.fromEntity(seller);
    }
}
