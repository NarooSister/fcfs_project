package com.sparta.orderservice.exception;

public record ApiErrorResponse(String errorCode, String errorMessage) {
}
