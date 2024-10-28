package com.sparta.orderservice.config;


import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 현재 요청에서 Authorization 헤더를 가져와 Feign 요청에 추가
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authHeader);
                }
            }
        };
    }
}
