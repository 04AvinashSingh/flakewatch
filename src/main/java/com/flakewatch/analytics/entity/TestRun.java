package com.flakewatch.analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_runs")
@Getter
@Setter
public class TestRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true, nullable = false, length = 100)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_metadata_id", nullable = false)
    private TestMetadata testMetadata;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "commit_hash", nullable = false, length = 40)
    private String commitHash;

    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt = LocalDateTime.now();

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
