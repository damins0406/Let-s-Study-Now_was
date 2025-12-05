package com.team.LetsStudyNow_rg.global.auth;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUser extends User {
    private Member member;
    
    // 하위 호환을 위한 public 필드
    public Long id;
    public String username;
    public String email;

    // 생성자 1: Member 객체로 생성 (MyUserDetailsService용)
    public CustomUser(
            Member member,
            Collection<? extends GrantedAuthority> authorities
    ){
        super(member.getEmail(), member.getPassword(), authorities);
        this.member = member;
        
        // 하위 호환을 위해 필드 초기화
        this.id = member.getId();
        this.username = member.getUsername();
        this.email = member.getEmail();
    }

    // 생성자 2: JWT 토큰으로 생성 (JwtFilter용 - 하위 호환)
    public CustomUser(
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ){
        super(email, password, authorities);
        this.member = null;  // Member 객체 없음
        this.email = email;
    }

    public Member getMember() {
        return member;
    }
}
