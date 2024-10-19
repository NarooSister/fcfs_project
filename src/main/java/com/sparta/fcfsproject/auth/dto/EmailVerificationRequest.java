package com.sparta.fcfsproject.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationRequest {
    private String email;
    private String verificationCode;
}
