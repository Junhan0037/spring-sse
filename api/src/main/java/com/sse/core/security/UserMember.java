package com.sse.core.security;

import com.sse.domain.member.Member;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserMember extends User {

    private final Member member;

    public UserMember(Member member) {
        super(member.getEmail(), member.getPassword(), List.of(new SimpleGrantedAuthority(member.getRole().getKey())));
        this.member = member;
    }

}
