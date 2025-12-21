package com.ispilo.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponse {
    private String id;
    private String type; // "user" or "seller"
    private String name;
    private String avatar;
    private String bio;
    private Boolean isVerified;
    private Double rating;
    private String location;
}
