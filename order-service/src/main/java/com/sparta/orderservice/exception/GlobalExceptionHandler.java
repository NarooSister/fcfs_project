package com.sparta.orderservice.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 예상치 못한 에러를 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        // 직접 500 Internal Server Error 처리
        ApiErrorResponse errorResponse = new ApiErrorResponse("G-500", "예상치 못한 서버 오류가 발생했습니다.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // FeignException.NotFound 예외를 404 Not Found로 처리
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErrorResponse> handleFeignNotFoundException(FeignException.NotFound e) {
        log.error("Feign error occurred: {}", e.getMessage(), e);

        try {
            // JSON 응답에서 errorCode와 errorMessage를 추출
            JsonNode errorBody = objectMapper.readTree(e.contentUTF8());
            String errorCode = errorBody.get("errorCode").asText();
            String errorMessage = errorBody.get("errorMessage").asText();

            ApiErrorResponse errorResponse = new ApiErrorResponse(errorCode, errorMessage);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

        } catch (Exception parseException) {
            log.error("Error parsing Feign error response", parseException);
            throw e;
        }
    }

    // 유저 서비스에서 발생한 비즈니스 예외 처리
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
