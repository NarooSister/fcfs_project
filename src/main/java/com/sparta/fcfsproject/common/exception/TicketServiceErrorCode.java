package com.sparta.fcfsproject.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TicketServiceErrorCode {
    ALL_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "T-001", "티켓이 존재하지 않습니다."),
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "T-002", "해당 티켓이 존재하지 않습니다."),
    TICKET_SOLD_OUT(HttpStatus.BAD_REQUEST, "T-003", "Ticket sold out");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TicketServiceErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}