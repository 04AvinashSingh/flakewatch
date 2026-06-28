package com.flakewatch.analytics.service;

import com.flakewatch.analytics.entity.TestMetadata;
import com.flakewatch.analytics.entity.TestRun;
import com.flakewatch.analytics.repository.TestMetadataRepository;
import com.flakewatch.analytics.repository.TestRunRepository;
import com.flakewatch.ingestion.dto.TestResultDto;
import com.flakewatch.ingestion.dto.TestRunPayload;
import com.flakewatch.quarantine.dto.QuarantinedTestDto;
import com.flakewatch.quarantine.service.IQuarantineSseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlakeDetectionService {

    private final TestMetadataRepository metadataRepository;
    private final TestRunRepository testRunRepository;
    private final RedisOperations<String, String> redisTemplate;
    private final IQuarantineSseService sseService;
    private final CacheManager cacheManager;

    @Value("${flakewatch.analytics.flake-threshold:3}")
    private int flakeThreshold;

    private static final String REDIS_KEY_PREFIX = "test:last_status:";

    @Transactional
    public void processTestResult(TestRunPayload payload, TestResultDto result) {
        try {
            // 1. Get or Create Test Metadata
            TestMetadata metadata = metadataRepository.findByTestIdentifier(result.getTestIdentifier())
                    .orElseGet(() -> {
                        TestMetadata newMetadata = new TestMetadata();
                        newMetadata.setTestIdentifier(result.getTestIdentifier());
                        newMetadata.setSuiteName(result.getSuiteName());
                        return metadataRepository.save(newMetadata);
                    });

            // 2. Save the Test Run
            TestRun run = new TestRun();
            run.setTestMetadata(metadata);
            run.setEventId(payload.getEventId() + "-" + result.getTestIdentifier()); // Composite unique ID per test
            run.setStatus(result.getStatus());
            run.setCommitHash(payload.getCommitHash());
            run.setBranchName(payload.getBranchName());
            run.setDurationMs(result.getDurationMs());
            run.setErrorMessage(result.getErrorMessage());
            testRunRepository.saveAndFlush(run); // Force insert to trigger constraint violation immediately

            // 3. Flake Detection Logic (using Redis)
            if ("PASS".equals(result.getStatus()) || "FAIL".equals(result.getStatus())) {
                detectFlake(metadata, payload.getCommitHash(), result.getStatus());
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Idempotent hit: Duplicate event skipped for eventId={} testId={}", 
                payload.getEventId(), result.getTestIdentifier());
            // Safely ignored
        }
    }

    private void detectFlake(TestMetadata metadata, String currentCommit, String currentStatus) {
        String redisKey = REDIS_KEY_PREFIX + metadata.getId();
        
        // Value format: "STATUS:COMMIT_HASH"
        String lastStateStr = redisTemplate.opsForValue().get(redisKey);

        if (lastStateStr != null) {
            String[] parts = lastStateStr.split(":");
            String lastStatus = parts[0];
            String lastCommit = parts[1];

            // If the status flipped on the EXACT SAME COMMIT, it is definitively a flake.
            if (!currentStatus.equals(lastStatus) && currentCommit.equals(lastCommit)) {
                log.warn("🚨 FLAKE DETECTED! Test '{}' flipped from {} to {} on commit {}", 
                        metadata.getTestIdentifier(), lastStatus, currentStatus, currentCommit);
                
                metadata.setFlakeScore(metadata.getFlakeScore() + 1);
                
                // Check if it crosses the quarantine threshold
                if (metadata.getFlakeScore() >= flakeThreshold && !metadata.isQuarantined()) {
                    log.error("🛑 QUARANTINING TEST: '{}'. Reached flake score of {}", 
                            metadata.getTestIdentifier(), metadata.getFlakeScore());
                    metadata.setQuarantined(true);
                    
                    metadataRepository.save(metadata); // Save before emitting event
                    
                    QuarantinedTestDto dto = QuarantinedTestDto.builder()
                            .testIdentifier(metadata.getTestIdentifier())
                            .suiteName(metadata.getSuiteName())
                            .flakeScore(metadata.getFlakeScore())
                            .quarantinedAt(metadata.getUpdatedAt())
                            .build();
                            
                    sseService.notifyNewQuarantine(dto);
                    
                    if (cacheManager.getCache("quarantinedTests") != null) {
                        cacheManager.getCache("quarantinedTests").clear();
                    }
                } else {
                    metadataRepository.save(metadata);
                }
            }
        }

        // Update Redis with the new state. TTL of 7 days.
        redisTemplate.opsForValue().set(redisKey, currentStatus + ":" + currentCommit, Duration.ofDays(7));
    }
}
