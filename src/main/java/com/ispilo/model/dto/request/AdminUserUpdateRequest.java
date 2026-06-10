package com.ispilo.model.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserUpdateRequest {

    @Email(message = "Email must be valid")
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String name;
    private String bio;
    private String location;
    private Boolean isAdmin;
    private Boolean isVerified;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private Boolean profilePublic;
}
