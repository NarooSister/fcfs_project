package com.sparta.ticketservice.exception;

public record ApiErrorResponse(String errorCode, String errorMessage) {
}
