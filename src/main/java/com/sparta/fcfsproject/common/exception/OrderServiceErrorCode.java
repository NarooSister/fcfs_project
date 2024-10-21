package com.sparta.fcfsproject.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderServiceErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-001", "Order not found"),
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