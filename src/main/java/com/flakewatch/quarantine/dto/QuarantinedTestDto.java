package com.flakewatch.quarantine.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class QuarantinedTestDto {
    private String testIdentifier;
    private String suiteName;
    private int flakeScore;
    private LocalDateTime quarantinedAt;
}
