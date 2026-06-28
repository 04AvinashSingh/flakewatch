package com.flakewatch.analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_metadata")
@Getter
@Setter
public class TestMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_identifier", unique = true, nullable = false, length = 500)
    private String testIdentifier;

    @Column(name = "suite_name", nullable = false)
    private String suiteName;

    @Column(name = "is_quarantined")
    private boolean isQuarantined = false;

    @Column(name = "flake_score")
    private int flakeScore = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
