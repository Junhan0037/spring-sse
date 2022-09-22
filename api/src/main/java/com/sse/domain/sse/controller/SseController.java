package com.sse.domain.sse.controller;

import com.sse.domain.member.Member;
import com.sse.domain.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    @GetMapping(value = "/subscribe/{eventName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<ResponseBodyEmitter> subscribe(@PathVariable String eventName) {
        Member member = Member.builder()
                .email("admin@email.com")
                .build();

        SseEmitter emitter = sseService.subscribe(member.getEmail(), eventName);

        return ResponseEntity.ok(emitter);
    }

}
