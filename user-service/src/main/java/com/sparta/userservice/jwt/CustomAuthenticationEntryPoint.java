package com.sparta.userservice.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 응답 타입 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized

        // 에러 메시지 작성 (errorCode, errorMessage 형식으로)
        String errorResponse = "{\"errorCode\": \"AUTH-002\", \"errorMessage\": \"권한이 없습니다. 유효한 자격 증명을 제공해주세요.\"}";

        // 에러 메시지를 클라이언트에 전달
        response.getWriter().write(errorResponse);
        response.getWriter().flush();  // 버퍼를 비워 즉시 전송
    }
}