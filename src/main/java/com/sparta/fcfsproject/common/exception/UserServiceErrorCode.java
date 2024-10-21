package com.sparta.fcfsproject.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum UserServiceErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U-001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER(HttpStatus.BAD_REQUEST, "U-002", "중복된 사용자 아이디 입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U-003", "비밀번호 확인이 일치하지 않습니다."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "U-004", "이미 등록된 이메일입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "U-005", "인증 코드가 맞지 않습니다."),
    PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "U-006", "현재 비밀번호가 맞지 않습니다."),
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U-007", "인증된 사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    UserServiceErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
