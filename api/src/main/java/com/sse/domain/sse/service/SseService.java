package com.sse.domain.sse.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sse.core.config.LocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final LocalDateTimeSerializer localDateTimeSerializer;

    private static final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private static final Map<String, String> lastSseData = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String groupId, String eventName) {
        Optional<SseEmitter> sseEmitterOpt = getSseEmitter(groupId, eventName);

        if (sseEmitterOpt.isPresent()) return sseEmitterOpt.get();

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> removeSseEmitter(groupId, eventName));
        emitter.onTimeout(() -> removeSseEmitter(groupId, eventName));
        emitter.onError((e) -> {
            e.printStackTrace();
            removeSseEmitter(groupId, eventName);
        });

        addSseEmitter(groupId, eventName, emitter);

        return emitter;
    }

    public void sendData(String groupId, String eventName, Object result) {
        Gson gson = localDateTimeSerializer.getGson();
        sendData(groupId, eventName, gson.toJson(result));
    }

    private void sendData(String groupId, String eventName, String data) {
        Optional<SseEmitter> sseEmitterOpt = getSseEmitter(groupId, eventName);

        if (sseEmitterOpt.isEmpty()) return;

        SseEmitter emitter = sseEmitterOpt.get();

        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name(eventName)
                .data(data);

        try {
            emitter.send(event);
            lastSseData.put(getKey(groupId, eventName), data);
            Thread.sleep(200);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            emitter.completeWithError(e);
        }
    }

    public void complete(String groupId, String eventName) {
        Optional<SseEmitter> sseEmitterOpt = getSseEmitter(groupId, eventName);
        if (sseEmitterOpt.isEmpty()) return;

        sseEmitterOpt.get().complete();
    }

    private Optional<SseEmitter> getSseEmitter(String groupId, String eventName) {
        return Optional.ofNullable(emitterMap.get(getKey(groupId, eventName)));
    }

    private void removeSseEmitter(String groupId, String eventName) {
        emitterMap.remove(getKey(groupId, eventName));
    }

    private void addSseEmitter(String groupId, String eventName, SseEmitter emitter) {
        emitterMap.put(getKey(groupId, eventName), emitter);
    }

    private String getKey(String groupId, String eventName) {
        return groupId + "_" + eventName;
    }

}
