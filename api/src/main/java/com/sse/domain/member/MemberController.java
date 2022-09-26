package com.sse.domain.member;

import com.sse.domain.job.dto.JobDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/test/{eventName}")
    public ResponseEntity<?> findAllMembers(@PathVariable String eventName) {
        Member member = Member.builder()
                .email("admin@email.com")
                .build();

        JobDTO jobDTO = memberService.findAllMembers(member, eventName);

        return ResponseEntity.ok(jobDTO);
    }

}
