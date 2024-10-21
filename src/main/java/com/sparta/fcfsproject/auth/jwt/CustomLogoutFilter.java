package com.sparta.fcfsproject.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public CustomLogoutFilter(JWTUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // Path and method verification
        if (!"/logout".equals(request.getRequestURI()) || !"POST".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }

        // null 체크
        if (refresh == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String username = jwtUtil.getUsername(refresh);
        String sessionId = jwtUtil.getSessionId(refresh);  // sessionId 가져오기
        String redisKey = username + ":" + sessionId;  // Redis에서 email과 sessionId를 조합한 키 사용

        // Redis에 저장되어 있는지 확인
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //로그아웃 진행
        redisTemplate.delete(redisKey);

        //Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // JWT의 만료 시간을 가져와서 현재 시간과의 차이를 TTL로 설정
        Long expirationTime = jwtUtil.getExpiration(refresh);
        Long ttl = expirationTime - System.currentTimeMillis();

        // 남은 TTL 만큼 Redis에 블랙리스트로 저장
        if (ttl > 0) {
            redisTemplate.opsForValue().set(refresh, "true", ttl, TimeUnit.MILLISECONDS);
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
