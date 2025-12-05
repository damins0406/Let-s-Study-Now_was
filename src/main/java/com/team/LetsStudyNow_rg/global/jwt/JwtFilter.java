package com.team.LetsStudyNow_rg.global.jwt;

import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j; // 로그 확인용
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String jwtToken = null;

        // 1. Authorization 헤더에서 토큰 추출 (우선 순위 1)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7); // "Bearer " 제거
        }

        // 2. 헤더에 없으면 쿠키에서 확인 (우선 순위 2 - 기존 로직)
        if (jwtToken == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 토큰이 없으면 그냥 통과 (로그인 안 된 상태로 처리됨)
        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 토큰 검증 및 인증 객체 생성
        try {
            Claims claims = JwtUtil.extractToken(jwtToken);

            // 권한 정보 파싱
            var arr = claims.get("authorities").toString().split(",");
            var authorities = Arrays.stream(arr).map(SimpleGrantedAuthority::new).toList();

            // CustomUser 생성
            CustomUser customUser = new CustomUser(
                    claims.get("email").toString(),
                    "none",
                    authorities
            );
            customUser.username = claims.get("username").toString();
            customUser.email = claims.get("email").toString();

            Number n = claims.get("id", Number.class);
            if (n != null) customUser.id = n.longValue();

            // SecurityContext에 저장
            var authToken = new UsernamePasswordAuthenticationToken(
                    customUser,
                    null,
                    authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            // 토큰이 유효하지 않아도 에러를 터트리지 않고 필터를 진행시킴 (401 처리는 시큐리티 설정에 맡김)
        }

        filterChain.doFilter(request, response);
    }
}