package com.ispilo.model.dto.response;

import com.ispilo.model.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private String id;
    private String targetId;
    private String targetType;
    private String reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
