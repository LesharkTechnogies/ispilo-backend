package com.ispilo.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFlagRequest {
    private Boolean flagged;
    private String reason;
    private Integer blockHours;
}
