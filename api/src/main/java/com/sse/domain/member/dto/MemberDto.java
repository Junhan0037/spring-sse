package com.sse.domain.member.dto;

import com.sse.domain.member.Role;
import lombok.Data;

public class MemberDto {

    @Data
    public static class LoginSuccessForm {
        private String email;
        private String name;
        private Role role;
    }

}
