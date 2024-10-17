package com.sparta.fcfsproject.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDto {
    private String email;
    private String password;
}
