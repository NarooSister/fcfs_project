package com.sparta.ticketservice.exception;

import lombok.Getter;

@Getter
public class TicketBusinessException extends RuntimeException {
    private final TicketServiceErrorCode errorCode;

    public TicketBusinessException(TicketServiceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
