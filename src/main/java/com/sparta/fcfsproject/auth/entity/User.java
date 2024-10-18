package com.sparta.fcfsproject.auth.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "이메일 주소가 유효하지 않습니다.")
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @Pattern(regexp = "\\d{2,3}-\\d{3,4}-\\d{4}", message = "전화번호 형식이 유효하지 않습니다.")
    private String phoneNumber;

    private String address;

    private String role;

    public User(String email, String role) {
        this.email = email;
        this.role = role;
    }
}
