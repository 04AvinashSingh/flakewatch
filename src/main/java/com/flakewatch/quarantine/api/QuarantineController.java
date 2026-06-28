package com.flakewatch.quarantine.api;

import com.flakewatch.analytics.repository.TestMetadataRepository;
import com.flakewatch.quarantine.dto.QuarantinedTestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.flakewatch.analytics.entity.TestMetadata;
import com.flakewatch.quarantine.service.IQuarantineSseService;

@RestController
@RequestMapping("/api/v1/quarantine")
@RequiredArgsConstructor
public class QuarantineController {

    private final TestMetadataRepository metadataRepository;
    private final IQuarantineSseService sseService;

    @GetMapping(path = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQuarantineEvents() {
        return sseService.subscribe();
    }

    @GetMapping
    public ResponseEntity<Page<QuarantinedTestDto>> getQuarantinedTests(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<TestMetadata> metadataPage;
        if (search != null && !search.trim().isEmpty()) {
            metadataPage = metadataRepository.searchQuarantinedTests(search.trim(), pageRequest);
        } else {
            metadataPage = metadataRepository.findByIsQuarantined(true, pageRequest);
        }

        Page<QuarantinedTestDto> response = metadataPage.map(metadata -> QuarantinedTestDto.builder()
                .testIdentifier(metadata.getTestIdentifier())
                .suiteName(metadata.getSuiteName())
                .flakeScore(metadata.getFlakeScore())
                .quarantinedAt(metadata.getUpdatedAt())
                .build());

        return ResponseEntity.ok(response);
    }
}
