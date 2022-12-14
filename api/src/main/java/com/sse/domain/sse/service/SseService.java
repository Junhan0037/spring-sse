package com.sse.domain.sse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sse.core.exception.BaseException;
import com.sse.domain.job.constant.JobStatus;
import com.sse.domain.sse.dto.SseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.sse.core.exception.ErrorType.JSON_FAILED;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final ObjectMapper objectMapper;

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

    public void sendData(String groupId, String eventName, Object result, JobStatus jobStatus) {
        // status 추가
        SseDTO sseDTO = SseDTO.builder()
                .result(result)
                .status(jobStatus)
                .build();

        try {
            String data = objectMapper.writeValueAsString(sseDTO);
            sendData(groupId, eventName, data);
        } catch (JsonProcessingException e) {
            throw new BaseException(JSON_FAILED, e);
        }
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

    private Optional<SseEmitter> getSseEmitter(String groupId, String eventName) {
        return Optional.ofNullable(emitterMap.get(getKey(groupId, eventName)));
    }

    private void addSseEmitter(String groupId, String eventName, SseEmitter emitter) {
        emitterMap.put(getKey(groupId, eventName), emitter);
    }

    private void removeSseEmitter(String groupId, String eventName) {
        emitterMap.remove(getKey(groupId, eventName));
    }

    public void completeSseEmitter(String groupId, String eventName) {
        Optional<SseEmitter> sseEmitterOpt = getSseEmitter(groupId, eventName);
        if (sseEmitterOpt.isEmpty()) return;

        sseEmitterOpt.get().complete();
    }

    private String getKey(String groupId, String eventName) {
        return groupId + "_" + eventName;
    }

}
