package com.team.LetsStudyNow_rg.global.jwt;

import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    private static SecretKey key;

    @Value("${jwt.secret.key}")
    private String secret;

    @PostConstruct
    public void init() {
        byte[] byteKey = Decoders.BASE64.decode(this.secret);
        JwtUtil.key = Keys.hmacShaKeyFor(byteKey);
    }

    // JWT 만들어주는 함수
    public static String createToken(Authentication auth) {
        var user = (CustomUser) auth.getPrincipal();
        // ["권한1", "권한2"] - > "권한1 권한2"
        var authorities = auth.getAuthorities().stream().map(a -> a.getAuthority())
                .collect(Collectors.joining(","));

        String jwt = Jwts.builder()
                .claim("username", user.username)
                .claim("email", user.email)
                .claim("id", user.id)
                .claim("authorities", authorities)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3600_000)) //유효기간
                .signWith(key)
                .compact();
        return jwt;
    }

    // JWT 까주는 함수
    public static Claims extractToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return claims;
    }
}
