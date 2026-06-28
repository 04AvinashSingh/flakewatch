package com.flakewatch.quarantine.api;

import com.flakewatch.analytics.entity.TestMetadata;
import com.flakewatch.analytics.repository.TestMetadataRepository;
import com.flakewatch.quarantine.dto.QuarantinedTestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.flakewatch.quarantine.service.IQuarantineSseService;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(controllers = QuarantineController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class QuarantineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestMetadataRepository metadataRepository;

    @MockBean
    private IQuarantineSseService sseService;

    @Test
    void getQuarantinedTests_ShouldReturnPaginatedResults() throws Exception {
        // Arrange
        TestMetadata metadata = new TestMetadata();
        metadata.setTestIdentifier("com.example.FlakyTest");
        metadata.setSuiteName("FlakySuite");
        metadata.setFlakeScore(5);
        metadata.setQuarantined(true);
        metadata.setUpdatedAt(LocalDateTime.now());

        Page<TestMetadata> mockPage = new PageImpl<>(List.of(metadata));
        
        when(metadataRepository.findByIsQuarantined(org.mockito.ArgumentMatchers.eq(true), any(Pageable.class))).thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/quarantine")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].testIdentifier").value("com.example.FlakyTest"))
                .andExpect(jsonPath("$.content[0].flakeScore").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
