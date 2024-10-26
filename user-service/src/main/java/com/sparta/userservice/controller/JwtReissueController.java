package com.sparta.userservice.controller;

import com.sparta.userservice.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
public class JwtReissueController {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public JwtReissueController(JWTUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {

        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 refresh 토큰을 가져옴
        String refresh = getRefreshTokenFromCookies(request);
        if (refresh == null) {
            return new ResponseEntity<>("Refresh token is missing", HttpStatus.BAD_REQUEST);
        }

        // 2. 토큰 유효성 검증
        try {
            if (!jwtUtil.validateToken(refresh)) {
                return new ResponseEntity<>("Invalid refresh token", HttpStatus.UNAUTHORIZED);
            }
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("Refresh token expired", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String sessionId = jwtUtil.getSessionId(refresh);
        String role = jwtUtil.getRole(refresh);
        String redisKey = username + ":" + sessionId;

        // Redis에 해당 refresh 토큰이 없으면, 에러 응답 반환
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            return new ResponseEntity<>("Refresh token not found in Redis", HttpStatus.BAD_REQUEST);
        }

        // Redis에서 블랙리스트 확인
        if (Objects.equals(redisTemplate.opsForValue().get(refresh), "true")) {
            return new ResponseEntity<>("refresh token is blacklisted", HttpStatus.UNAUTHORIZED);
        }

        //make new JWT
        String newAccess = jwtUtil.createJwt(username, role, 600000L);
        String newRefresh = jwtUtil.createJwtWithSession(username, role, sessionId, 86400000L);  // 1 day

        // Redis에 저장된 기존 Refresh 토큰 삭제
        redisTemplate.delete(redisKey);

        // 새로운 Refresh 토큰 Redis에 저장
        redisTemplate.opsForValue().set(redisKey, newRefresh, 86400000L, TimeUnit.MILLISECONDS);

        //response
        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 쿠키에서 Refresh 토큰 추출
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
