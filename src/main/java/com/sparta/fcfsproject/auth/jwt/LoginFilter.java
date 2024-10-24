package com.sparta.fcfsproject.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        //유저 정보
        String username = authentication.getName();
        String sessionId = request.getSession().getId();  // 세션 ID 가져오기

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt(username, role, 600000L);  // 10분
        String refresh = jwtUtil.createJwtWithSession(username, role, sessionId, 86400000L);  // 1일 만료, sessionId 포함

        //Redis에 Refresh 토큰 저장 (sessionId를 포함)
        redisTemplate.opsForValue().set(username + ":" + sessionId, refresh, 86400000L, TimeUnit.MILLISECONDS);

        //응답 설정
        response.setHeader("Authorization", "Bearer " +access);   // 응답 해더에 넣음
        response.addCookie(createCookie("refresh", refresh));   // 응답 쿠키에 넣음
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());  // 401 상태 코드
        response.setContentType("application/json;charset=UTF-8");

        // 에러 응답을 JSON 형식으로 설정 (errorCode, errorMessage)
        String errorResponse = "{\"errorCode\": \"AUTH-001\", \"errorMessage\": \"정확하지 않은 아이디 또는 비밀번호 입니다.\"}";

        // 에러 메시지를 클라이언트에 전달
        response.getWriter().write(errorResponse);
        response.getWriter().flush();  // 버퍼를 비워 즉시 전송
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);   //Https로 바꾸면
        //cookie.setPath("/");
        cookie.setHttpOnly(true);   //js에서 접근 못하도록 막음
        return cookie;
    }
}