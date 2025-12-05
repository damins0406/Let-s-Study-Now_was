package com.team.LetsStudyNow_rg.global.config;

import com.team.LetsStudyNow_rg.global.jwt.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 동작
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // csrf 기능 잠시 해제
        http.csrf((csrf) -> csrf.disable());
        // 세션 데이터 생성하지 않음
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.addFilterBefore(new JwtFilter(), ExceptionTranslationFilter.class);

        // 요청권한 일시 모두 허용
        http.authorizeHttpRequests((authorize) ->
                authorize.requestMatchers("/**").permitAll()
        );

        // API 인증 예외 처리
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.")
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다.")
                )
        );

        // 로그아웃 jwt
        http.logout(logout -> logout
                .logoutUrl("/api/logout") // 로그아웃 처리 URL
                .logoutSuccessHandler((req, res, auth) -> { // 성공 시 200 OK 응답
                    res.setStatus(HttpServletResponse.SC_OK);
                })
                .deleteCookies("jwt")
                .permitAll()
        );

        return http.build();

    }
}
