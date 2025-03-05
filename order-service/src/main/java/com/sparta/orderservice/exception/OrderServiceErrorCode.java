package com.sparta.orderservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderServiceErrorCode {
    ALL_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-001", "주문 내역이 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O-002", "해당하는 주문이 없습니다."),
    TICKET_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "O-003", "판매 중이 아닌 티켓입니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "O-004", "재고가 충분하지 않습니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "O-005", "주문을 취소할 수 없습니다."),
    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "O-006", "티켓을 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "O-007", "장바구니에 담긴 상품이 없습니다."),
    CART_NOT_FOUND_TICKET(HttpStatus.NOT_FOUND, "O-008", "해당하는 티켓을 찾을 수 없습니다."),
    CANNOT_COMPLETE_ORDER(HttpStatus.NOT_FOUND, "O-009", "주문 확정된 티켓이 아닙니다."),
    INVALID_TICKET(HttpStatus.NOT_FOUND, "O-010", "유효하지 않은 티켓입니다."),
    CANNOT_CONFIRM_ORDER(HttpStatus.NOT_FOUND, "O-011", "주문 대기 중인 티켓이 아닙니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "O-012", "결제가 실패하였습니다."),
    INVALID_PENDING_ORDER_STATUS(HttpStatus.NOT_FOUND, "O-013", "PendingOrder가 유효하지 않습니다."),
    INVALID_PENDING_STOCK(HttpStatus.NOT_FOUND, "O-014", "예약된 재고가 없습니다."),
    INVALID_ORDER_REQUEST(HttpStatus.NOT_IMPLEMENTED, "O-015", "유효하지 않은 OrderRequest 입니다."),
    PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "O-016", "가격이 정확하지 않습니다."),
    CONCURRENT_ACCESS(HttpStatus.BAD_REQUEST, "O-017", "락을 획득하지 못했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    OrderServiceErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}