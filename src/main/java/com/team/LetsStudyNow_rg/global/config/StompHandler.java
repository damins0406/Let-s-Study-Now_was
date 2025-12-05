package com.team.LetsStudyNow_rg.global.config;

import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import com.team.LetsStudyNow_rg.global.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class StompHandler implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 연결 요청(CONNECT)일 때만 토큰 검증
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 헤더에서 "Authorization" 값 꺼내기
            String authorization = accessor.getFirstNativeHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new IllegalArgumentException("토큰이 없거나 형식이 잘못되었습니다.");
            }

            String token = authorization.substring(7);

            try {
                // 토큰 검증
                Claims claims = JwtUtil.extractToken(token);

                // 권한 정보 파싱 (JwtFilter 로직과 동일)
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

                // 인증 객체 생성 및 주입
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(customUser, null, authorities);

                accessor.setUser(auth); // ★ 핵심: WebSocket Session에 유저 정보 저장

            } catch (Exception e) {
                log.error("WebSocket 인증 실패", e);
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
        }
        return message;
    }
}