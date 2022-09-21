package com.sse.core.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sse.core.security.UserMember;
import com.sse.domain.member.Member;
import com.sse.domain.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        UserMember userMember = (UserMember) authentication.getPrincipal();
        Member member = userMember.getMember();
        MemberDto.LoginSuccessForm loginSuccessForm = modelMapper.map(member, MemberDto.LoginSuccessForm.class);

        objectMapper.writeValue(response.getWriter(), loginSuccessForm);
    }

}
