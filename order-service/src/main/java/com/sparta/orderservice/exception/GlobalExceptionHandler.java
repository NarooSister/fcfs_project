package com.sparta.orderservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // 1. 예상치 못한 에러를 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        // 직접 500 Internal Server Error 처리
        ApiErrorResponse errorResponse = new ApiErrorResponse("G-500", "예상치 못한 서버 오류가 발생했습니다.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 2. 유저 서비스에서 발생한 비즈니스 예외 처리
    @ExceptionHandler(OrderBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleUserBusinessException(OrderBusinessException e) {
        return buildErrorResponse(e.getErrorCode());
    }

    // 유저 에러 응답 생성 메서드
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(OrderServiceErrorCode errorCode) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }
}
