package com.flakewatch.analytics.kafka;

import com.flakewatch.analytics.service.FlakeDetectionService;
import com.flakewatch.ingestion.dto.TestRunPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultConsumer {

    private final FlakeDetectionService flakeDetectionService;

    @KafkaListener(topics = "test-results", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTestRunPayload(TestRunPayload payload) {
        log.info("Received Kafka payload for commit: {} with {} test results", 
                payload.getCommitHash(), payload.getResults().size());

        try {
            // Process each test result in the payload
            payload.getResults().forEach(result -> 
                flakeDetectionService.processTestResult(payload, result)
            );
            
            log.debug("Successfully processed payload for commit: {}", payload.getCommitHash());
        } catch (Exception e) {
            log.error("Error processing test run payload for commit: {}", payload.getCommitHash(), e);
            // In a production system, implement retry logic or a Dead Letter Topic (DLT) here
        }
    }
}
