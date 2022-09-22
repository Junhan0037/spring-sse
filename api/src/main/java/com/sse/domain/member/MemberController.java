package com.sse.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/test")
    public ResponseEntity<?> findAllMembers() {
        Member member = Member.builder()
                .email("admin@email.com")
                .build();
        memberService.findAllMembers(member);
        return ResponseEntity.ok().build();
    }

}
