package com.sparta.userservice.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user", indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true)
})
public class User {
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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

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

    public void updatePassword(String password) {
        this.password = password;
    }
}
