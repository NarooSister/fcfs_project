package com.sparta.fcfsproject.auth.controller;

import com.sparta.fcfsproject.auth.jwt.JWTUtil;
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

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return new ResponseEntity<>("No cookies present", HttpStatus.BAD_REQUEST);
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {
            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String email = jwtUtil.getEmail(refresh);
        String sessionId = jwtUtil.getSessionId(refresh);
        String role = jwtUtil.getRole(refresh);
        String redisKey = email + ":" + sessionId;

        // Redis에 해당 refresh 토큰이 없으면, 에러 응답 반환
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            return new ResponseEntity<>("Refresh token not found in Redis", HttpStatus.BAD_REQUEST);
        }

        // Redis에서 블랙리스트 확인
        if (Objects.equals(redisTemplate.opsForValue().get(refresh), "true")) {
            return new ResponseEntity<>("refresh token is blacklisted", HttpStatus.UNAUTHORIZED);
        }

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", email, role, 600000L);
        String newRefresh = jwtUtil.createJwtWithSession("refresh", email, role, sessionId, 86400000L);  // 1 day

        // Redis에 저장된 기존 Refresh 토큰 삭제
        redisTemplate.delete(redisKey);

        // 새로운 Refresh 토큰 Redis에 저장
        redisTemplate.opsForValue().set(redisKey, newRefresh, 86400000L, TimeUnit.MILLISECONDS);

        //response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
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
