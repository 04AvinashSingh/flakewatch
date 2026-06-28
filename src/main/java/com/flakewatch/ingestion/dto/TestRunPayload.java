package com.flakewatch.ingestion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class TestRunPayload {

    @NotBlank(message = "Event ID cannot be blank")
    private String eventId;

    @NotBlank(message = "Commit hash cannot be blank")
    private String commitHash;

    @NotBlank(message = "Branch name cannot be blank")
    private String branchName;

    @NotBlank(message = "CI Runner ID cannot be blank")
    private String runnerId;

    @NotEmpty(message = "Test results list cannot be empty")
    @Valid
    private List<TestResultDto> results;

}
