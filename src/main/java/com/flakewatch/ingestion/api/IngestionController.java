package com.flakewatch.ingestion.api;

import com.flakewatch.ingestion.dto.TestRunPayload;
import com.flakewatch.ingestion.kafka.TestResultProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingest")
@RequiredArgsConstructor
@Slf4j
public class IngestionController {

    private final TestResultProducer testResultProducer;

    @PostMapping("/test-results")
    public ResponseEntity<Void> ingestTestResults(@Valid @RequestBody TestRunPayload payload) {
        log.info("Received test ingestion request from runner: {} for commit: {}", 
                payload.getRunnerId(), payload.getCommitHash());
        
        // Asynchronously publish to Kafka
        testResultProducer.publishTestResults(payload);
        
        // Return 202 Accepted immediately so we don't block the CI runner
        return ResponseEntity.accepted().build();
    }
}
