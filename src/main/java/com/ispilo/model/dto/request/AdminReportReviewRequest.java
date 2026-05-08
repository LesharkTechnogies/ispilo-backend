package com.ispilo.model.dto.request;

import com.ispilo.model.enums.AdminReportAction;
import com.ispilo.model.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReportReviewRequest {

    @NotNull(message = "Status is required")
    private ReportStatus status;

    @NotNull(message = "Action is required")
    private AdminReportAction action;

    // Optional: how long to block uploads (hours). Default handled in service.
    private Integer blockHours;

    private String note;
}
