package com.sparta.fcfsproject.auth.controller;

import com.sparta.fcfsproject.auth.dto.SignupRequest;
import com.sparta.fcfsproject.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/")
    public String index(){
        return "success";
    }

    // 로그인 로직은 LoginFilter에서 처리함 (/login)으로 접근 가능
    // 로그아웃 로직은 CustomLogoutFilter에서 처리함 (/logout)으로 접근 가능

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Validated SignupRequest signupRequest) {
        userService.signup(signupRequest);
        return new ResponseEntity<>("회원가입이 완료되었습니다.", HttpStatus.CREATED);
    }

    @GetMapping("/admin")
    public String adminP() {

        return "Admin Controller";
    }
}
