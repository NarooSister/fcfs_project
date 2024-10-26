package com.sparta.userservice.exception;

public record ApiErrorResponse(String errorCode, String errorMessage) {
}
