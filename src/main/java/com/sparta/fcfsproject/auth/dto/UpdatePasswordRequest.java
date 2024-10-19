package com.sparta.fcfsproject.auth.dto;

import lombok.Getter;

@Getter
public class UpdatePasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}
