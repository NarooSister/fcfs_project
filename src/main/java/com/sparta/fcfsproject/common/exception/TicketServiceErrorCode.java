package com.sparta.fcfsproject.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TicketServiceErrorCode {
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "T-001", "Ticket not found"),
    TICKET_SOLD_OUT(HttpStatus.BAD_REQUEST, "T-002", "Ticket sold out");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TicketServiceErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}