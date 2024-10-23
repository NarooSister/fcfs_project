package com.sparta.fcfsproject.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderServiceErrorCode {
    ALL_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-001", "주문 내역이 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-002", "해당하는 주문이 없습니다."),
    TICKET_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "O-003", "판매 중이 아닌 티켓입니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "O-004", "재고가 충분하지 않습니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "O-005", "주문을 취소할 수 없습니다."),
    TICKET_NOT_FOUND(HttpStatus.BAD_REQUEST, "O-006", "티켓을 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    OrderServiceErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}