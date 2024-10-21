package com.sparta.fcfsproject.common.exception;

import lombok.Getter;

@Getter
public class OrderBusinessException extends RuntimeException {
    private final OrderServiceErrorCode errorCode;

    public OrderBusinessException(OrderServiceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
