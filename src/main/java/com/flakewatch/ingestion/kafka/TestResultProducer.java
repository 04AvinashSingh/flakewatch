package com.flakewatch.ingestion.kafka;

import com.flakewatch.ingestion.dto.TestRunPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "test-results";

    public void publishTestResults(TestRunPayload payload) {
        log.info("Publishing test results for commit: {} to Kafka topic: {}", payload.getCommitHash(), TOPIC);
        
        // Use the commit hash as the Kafka key to ensure ordering of events for the same commit
        kafkaTemplate.send(TOPIC, payload.getCommitHash(), payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Successfully sent test run payload to partition {}", 
                                result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to send test run payload to Kafka", ex);
                        // In a real production system, we would persist this to a Dead Letter Queue (DLQ) or DB
                    }
                });
    }
}
