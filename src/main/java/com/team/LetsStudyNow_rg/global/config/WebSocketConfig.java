package com.team.LetsStudyNow_rg.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 클라이언트가 웹소켓 서버에 연결할 주소 (ws://localhost:8080/ws-stomp)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "https://*.vercel.app"
                )
                .withSockJS();

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. 구독 요청 (서버 -> 클라이언트)
        registry.enableSimpleBroker("/sub");

        // 3. 발행 요청 (클라이언트 -> 서버)
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}