package com.sparta.fcfsproject.common.exception;

import lombok.Getter;

@Getter
public class UserBusinessException extends RuntimeException {
    private final UserServiceErrorCode errorCode;

    public UserBusinessException(UserServiceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

