package com.sparta.fcfsproject.common.exception;

import lombok.Getter;

@Getter
public record ApiErrorResponse(String errorCode, String errorMessage) {
}
