package com.flakewatch.analytics.repository;

import com.flakewatch.analytics.entity.TestMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

@Repository
public interface TestMetadataRepository extends JpaRepository<TestMetadata, Long> {
    Optional<TestMetadata> findByTestIdentifier(String testIdentifier);
    
    Page<TestMetadata> findByIsQuarantined(boolean isQuarantined, Pageable pageable);
    
    @Query("SELECT t FROM TestMetadata t WHERE t.isQuarantined = true AND " +
           "(LOWER(t.testIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.suiteName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TestMetadata> searchQuarantinedTests(@Param("search") String search, Pageable pageable);
}
