package com.flakewatch.integration;

import com.flakewatch.analytics.entity.TestMetadata;
import com.flakewatch.analytics.repository.TestMetadataRepository;
import com.flakewatch.analytics.repository.TestRunRepository;
import com.flakewatch.ingestion.dto.TestResultDto;
import com.flakewatch.ingestion.dto.TestRunPayload;
import com.flakewatch.ingestion.kafka.TestResultProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.Disabled;

@Disabled("Requires Docker environment for Testcontainers")
@SpringBootTest
@Testcontainers
public class FlakewatchIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("flakewatch_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("flakewatch.analytics.flake-threshold", () -> 2); // Lower threshold for testing
    }

    @Autowired
    private TestResultProducer producer;

    @Autowired
    private TestMetadataRepository metadataRepository;

    @Autowired
    private TestRunRepository testRunRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        testRunRepository.deleteAll();
        metadataRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void shouldDetectFlakyTestAndQuarantine() throws Exception {
        String testId = "com.example.AuthTest.testLogin";
        String commitHash = "abc123commit";

        // 1. Simulate first run: PASS
        TestResultDto passResult = new TestResultDto();
        passResult.setTestIdentifier(testId);
        passResult.setSuiteName("AuthSuite");
        passResult.setStatus("PASS");
        passResult.setDurationMs(150L);

        TestRunPayload payload1 = new TestRunPayload();
        payload1.setCommitHash(commitHash);
        payload1.setBranchName("main");
        payload1.setRunnerId("runner-1");
        payload1.setResults(List.of(passResult));

        producer.publishTestResults(payload1);

        // Wait for Kafka consumer to process the PASS
        await().atMost(10, TimeUnit.SECONDS).until(() -> testRunRepository.count() == 1);

        // 2. Simulate second run (retry on SAME commit): FAIL
        TestResultDto failResult = new TestResultDto();
        failResult.setTestIdentifier(testId);
        failResult.setSuiteName("AuthSuite");
        failResult.setStatus("FAIL");
        failResult.setDurationMs(200L);
        failResult.setErrorMessage("NullPointerException at line 42");

        TestRunPayload payload2 = new TestRunPayload();
        payload2.setCommitHash(commitHash); // Same commit!
        payload2.setBranchName("main");
        payload2.setRunnerId("runner-1");
        payload2.setResults(List.of(failResult));

        producer.publishTestResults(payload2);

        // Wait for Kafka consumer to process the FAIL
        await().atMost(10, TimeUnit.SECONDS).until(() -> testRunRepository.count() == 2);

        // 3. Verify Flake Score incremented (Threshold is 2, so it shouldn't quarantine yet)
        TestMetadata metadata = metadataRepository.findByTestIdentifier(testId).orElseThrow();
        assertThat(metadata.getFlakeScore()).isEqualTo(1);
        assertThat(metadata.isQuarantined()).isFalse();

        // 4. Simulate third run: PASS again (Flake Score hits 2 -> Quarantine!)
        TestRunPayload payload3 = new TestRunPayload();
        payload3.setCommitHash(commitHash);
        payload3.setBranchName("main");
        payload3.setRunnerId("runner-1");
        payload3.setResults(List.of(passResult));

        producer.publishTestResults(payload3);

        await().atMost(10, TimeUnit.SECONDS).until(() -> testRunRepository.count() == 3);

        // 5. Verify Quarantined
        TestMetadata finalMetadata = metadataRepository.findByTestIdentifier(testId).orElseThrow();
        assertThat(finalMetadata.getFlakeScore()).isEqualTo(2);
        assertThat(finalMetadata.isQuarantined()).isTrue();
    }
}
