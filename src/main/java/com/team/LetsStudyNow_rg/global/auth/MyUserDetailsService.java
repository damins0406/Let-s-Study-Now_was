package com.team.LetsStudyNow_rg.global.auth;


import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        var member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("계정이 없습니다."));

        // 일반유저: ROLE_USER, 관리자: ROLE_ADMIN
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(member.getRole().name()));

        return new CustomUser(member, authorities);
    }
}
