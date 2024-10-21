package com.sparta.fcfsproject.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderServiceErrorCode {
    ALL_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-001", "주문 내역이 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-002", "해당하는 주문이 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O-002", "Invalid order status");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    OrderServiceErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}