package com.sparta.fcfsproject.auth.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "User", indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true)
})
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    private String name;

    private String phoneNumber;

    private String address;

    private String role;

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public User(String username, String email, String password, String name, String phoneNumber, String address, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
    }
    public void updateProfile(String phoneNumber, String address) {
        // phoneNumber가 null이 아닌 경우에만 업데이트
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        // address가 null이 아닌 경우에만 업데이트
        if (address != null) {
            this.address = address;
        }
    }
    public void updatePassword(String password){
        this.password = password;
    }
}
