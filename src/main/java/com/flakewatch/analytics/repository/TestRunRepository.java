package com.flakewatch.analytics.repository;

import com.flakewatch.analytics.entity.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    @Modifying
    @Query("DELETE FROM TestRun t WHERE t.executedAt < :cutoff")
    int deleteByExecutedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
