package com.sparta.userservice.dto;


import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {
    @Pattern(regexp = "\\d{2,3}-\\d{3,4}-\\d{4}", message = "전화번호 형식이 유효하지 않습니다.")
    private String phoneNumber;
    private String address;

    public UpdateProfileRequest(String phoneNumber, String address) {
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
