package com.flakewatch.quarantine.service;

import com.flakewatch.quarantine.dto.QuarantinedTestDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QuarantineSseService implements IQuarantineSseService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Infinite timeout
        
        try {
            // Send an initial connection event to prevent connection drop
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });
        emitter.onError((e) -> this.emitters.remove(emitter));

        return emitter;
    }

    public void notifyNewQuarantine(QuarantinedTestDto dto) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("QUARANTINE_ADDED")
                        .data(dto));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
