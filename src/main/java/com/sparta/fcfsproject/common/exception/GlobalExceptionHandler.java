package com.sparta.fcfsproject.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // 1. 예상치 못한 에러를 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception e) {
        // 직접 500 Internal Server Error 처리
        ApiErrorResponse errorResponse = new ApiErrorResponse("G-500", "Internal server error occurred");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    // 2. 각 서비스에서 발생한 비즈니스 예외 처리 (msa로 변경한 뒤)
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException e) {
//        return buildErrorResponse(e.getErrorCode());
//    }

    // 2. 유저 서비스에서 발생한 비즈니스 예외 처리
    @ExceptionHandler(UserBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleUserBusinessException(UserBusinessException e) {
        return buildErrorResponse(e.getErrorCode());
    }

    // 3. 주문 서비스에서 발생한 비즈니스 예외 처리
    @ExceptionHandler(OrderBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderBusinessException(OrderBusinessException e) {
        return buildErrorResponse(e.getErrorCode());
    }

    // 4. 티켓 서비스에서 발생한 비즈니스 예외 처리
    @ExceptionHandler(TicketBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderBusinessException(TicketBusinessException e) {
        return buildErrorResponse(e.getErrorCode());
    }

    // 유저 에러 응답 생성 메서드
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(UserServiceErrorCode errorCode) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

    // 오더 서비스용 에러 응답 생성
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(OrderServiceErrorCode errorCode) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

    // 티켓 서비스용 에러 응답 생성
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(TicketServiceErrorCode errorCode) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(errorCode.getCode(), errorCode.getMessage());
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

}
