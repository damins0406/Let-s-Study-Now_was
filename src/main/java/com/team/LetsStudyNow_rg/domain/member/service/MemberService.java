package com.team.LetsStudyNow_rg.domain.member.service;

import com.team.LetsStudyNow_rg.domain.member.dto.request.LoginDto;
import com.team.LetsStudyNow_rg.domain.member.dto.request.RegisterDto;
import com.team.LetsStudyNow_rg.domain.member.dto.response.ProfileDto;
import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.enums.Role;
import com.team.LetsStudyNow_rg.domain.member.exception.DuplicateEmailException;
import com.team.LetsStudyNow_rg.domain.member.exception.DuplicateUsernameException;
import com.team.LetsStudyNow_rg.domain.member.exception.MemberNotFoundException;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import com.team.LetsStudyNow_rg.global.jwt.JwtUtil;
import com.team.LetsStudyNow_rg.global.s3.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final S3Service s3Service;

    // 기본 프로필 url
    @Value("${custom.s3.default-image-url}")
    private String defaultProfileImageUrl;

    final int ACCESS_TIME = 60 * 60;

    // 로그인 로직
    @Transactional
    public String loginService(LoginDto req, HttpServletResponse response) {
        var authToken = new UsernamePasswordAuthenticationToken(req.email(), req.password());
        var auth = authenticationManagerBuilder.getObject().authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        var auth2 = SecurityContextHolder.getContext().getAuthentication();
        var jwt = JwtUtil.createToken(auth2);

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .maxAge(ACCESS_TIME)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return jwt;
    }

    // 회원가입 로직
    @Transactional
    public void registerService(RegisterDto req) {
        // 커스텀 예외 적용
        if (memberRepository.existsByEmail(req.email())) {
            throw new DuplicateEmailException(req.email());
        }
        if (memberRepository.existsByUsername(req.username())) {
            throw new DuplicateUsernameException(req.username());
        }

        String encodePw = passwordEncoder.encode(req.password());

        String profileImageUrl = defaultProfileImageUrl;

        Member member = Member.builder()
                .username(req.username())
                .email(req.email())
                .password(encodePw)
                .role(Role.ROLE_USER)
                .profileImage(profileImageUrl)
                .studyField(req.studyField())
                .bio(req.bio())
                .build();

        memberRepository.save(member);
    }

    // 마이프로필 로직
    @Transactional(readOnly = true)
    public ProfileDto profileService(CustomUser customUser) {
        // 커스텀 예외 적용
        Member user = memberRepository.findById(customUser.id)
                .orElseThrow(() -> new MemberNotFoundException(customUser.id));

        return new ProfileDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getProfileImage(),
                user.getStudyField(),
                user.getBio(),
                user.getLevel()
        );
    }
}