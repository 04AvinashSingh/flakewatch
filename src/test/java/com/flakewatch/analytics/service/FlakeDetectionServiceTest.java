package com.flakewatch.analytics.service;

import com.flakewatch.analytics.entity.TestMetadata;
import com.flakewatch.analytics.repository.TestMetadataRepository;
import com.flakewatch.analytics.repository.TestRunRepository;
import com.flakewatch.ingestion.dto.TestResultDto;
import com.flakewatch.ingestion.dto.TestRunPayload;
import com.flakewatch.quarantine.dto.QuarantinedTestDto;
import com.flakewatch.quarantine.service.IQuarantineSseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlakeDetectionServiceTest {

    @Mock
    private TestMetadataRepository metadataRepository;

    @Mock
    private TestRunRepository testRunRepository;

    @Mock
    private RedisOperations<String, String> redisTemplate;

    @Mock
    private IQuarantineSseService sseService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Cache cache;

    @InjectMocks
    private FlakeDetectionService flakeDetectionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(flakeDetectionService, "flakeThreshold", 2);
    }

    @Test
    void processTestResult_ShouldCreateMetadataIfNotFound() {
        // Arrange
        TestRunPayload payload = new TestRunPayload();
        payload.setEventId("event-1");
        payload.setCommitHash("commit-1");

        TestResultDto result = new TestResultDto();
        result.setTestIdentifier("com.test.MyTest");
        result.setStatus("PASS");

        when(metadataRepository.findByTestIdentifier("com.test.MyTest")).thenReturn(Optional.empty());
        when(metadataRepository.save(any(TestMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        flakeDetectionService.processTestResult(payload, result);

        // Assert
        verify(metadataRepository).save(any(TestMetadata.class));
        verify(testRunRepository).saveAndFlush(any());
        verify(valueOperations).set(eq("test:last_status:null"), eq("PASS:commit-1"), any(Duration.class));
    }

    @Test
    void processTestResult_ShouldDetectFlake_WhenStatusFlipsOnSameCommit() {
        // Arrange
        TestMetadata metadata = new TestMetadata();
        metadata.setId(1L);
        metadata.setTestIdentifier("com.test.MyTest");
        metadata.setFlakeScore(0);
        metadata.setQuarantined(false);

        TestRunPayload payload = new TestRunPayload();
        payload.setEventId("event-1");
        payload.setCommitHash("commit-1");

        TestResultDto result = new TestResultDto();
        result.setTestIdentifier("com.test.MyTest");
        result.setStatus("FAIL"); // Flipped from PASS

        when(metadataRepository.findByTestIdentifier("com.test.MyTest")).thenReturn(Optional.of(metadata));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:last_status:1")).thenReturn("PASS:commit-1");

        // Act
        flakeDetectionService.processTestResult(payload, result);

        // Assert
        assertThat(metadata.getFlakeScore()).isEqualTo(1); // Incremented
        verify(metadataRepository).save(metadata);
        verify(valueOperations).set(eq("test:last_status:1"), eq("FAIL:commit-1"), any(Duration.class));
    }

    @Test
    void processTestResult_ShouldQuarantine_WhenThresholdReached() {
        // Arrange
        TestMetadata metadata = new TestMetadata();
        metadata.setId(1L);
        metadata.setTestIdentifier("com.test.MyTest");
        metadata.setSuiteName("MySuite");
        metadata.setFlakeScore(1); // One away from threshold (2)
        metadata.setQuarantined(false);
        metadata.setUpdatedAt(LocalDateTime.now());

        TestRunPayload payload = new TestRunPayload();
        payload.setEventId("event-1");
        payload.setCommitHash("commit-1");

        TestResultDto result = new TestResultDto();
        result.setTestIdentifier("com.test.MyTest");
        result.setStatus("PASS"); // Flipped from FAIL

        when(metadataRepository.findByTestIdentifier("com.test.MyTest")).thenReturn(Optional.of(metadata));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:last_status:1")).thenReturn("FAIL:commit-1");
        when(cacheManager.getCache("quarantinedTests")).thenReturn(cache);

        // Act
        flakeDetectionService.processTestResult(payload, result);

        // Assert
        assertThat(metadata.getFlakeScore()).isEqualTo(2);
        assertThat(metadata.isQuarantined()).isTrue();
        
        verify(metadataRepository).save(metadata);
        
        ArgumentCaptor<QuarantinedTestDto> dtoCaptor = ArgumentCaptor.forClass(QuarantinedTestDto.class);
        verify(sseService).notifyNewQuarantine(dtoCaptor.capture());
        assertThat(dtoCaptor.getValue().getTestIdentifier()).isEqualTo("com.test.MyTest");
        
        verify(cache).clear(); // Cache eviction triggered
    }

    @Test
    void processTestResult_ShouldNotFlake_WhenStatusChangesOnDifferentCommit() {
        // Arrange
        TestMetadata metadata = new TestMetadata();
        metadata.setId(1L);
        metadata.setTestIdentifier("com.test.MyTest");
        metadata.setFlakeScore(0);

        TestRunPayload payload = new TestRunPayload();
        payload.setEventId("event-1");
        payload.setCommitHash("commit-2"); // New commit

        TestResultDto result = new TestResultDto();
        result.setTestIdentifier("com.test.MyTest");
        result.setStatus("FAIL"); 

        when(metadataRepository.findByTestIdentifier("com.test.MyTest")).thenReturn(Optional.of(metadata));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:last_status:1")).thenReturn("PASS:commit-1"); // Previous commit

        // Act
        flakeDetectionService.processTestResult(payload, result);

        // Assert
        assertThat(metadata.getFlakeScore()).isEqualTo(0); // Not a flake, just a normal regression
        verify(valueOperations).set(eq("test:last_status:1"), eq("FAIL:commit-2"), any(Duration.class));
    }
}
