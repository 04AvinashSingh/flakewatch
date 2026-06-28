package com.flakewatch.quarantine.service;

import com.flakewatch.quarantine.dto.QuarantinedTestDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IQuarantineSseService {
    SseEmitter subscribe();
    void notifyNewQuarantine(QuarantinedTestDto dto);
}
