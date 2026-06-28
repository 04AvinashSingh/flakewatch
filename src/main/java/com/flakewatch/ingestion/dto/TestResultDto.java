package com.flakewatch.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestResultDto {

    @NotBlank(message = "Test identifier cannot be blank")
    private String testIdentifier; // e.g., com.flakewatch.UserServiceTest.testLogin

    @NotBlank(message = "Suite name cannot be blank")
    private String suiteName;

    @NotBlank(message = "Status cannot be blank")
    private String status; // PASS, FAIL, SKIPPED

    @NotNull(message = "Duration cannot be null")
    private Long durationMs;

    private String errorMessage; // Optional, only if failed
}
