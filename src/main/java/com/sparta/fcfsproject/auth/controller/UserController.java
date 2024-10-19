package com.sparta.fcfsproject.auth.controller;

import com.sparta.fcfsproject.auth.config.AuthFacade;
import com.sparta.fcfsproject.auth.dto.*;
import com.sparta.fcfsproject.auth.entity.User;
import com.sparta.fcfsproject.auth.service.EmailService;
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
    private final EmailService emailService;

    @GetMapping("/")
    public String index(){
        return "success";
    }

    // 로그인 로직은 LoginFilter에서 처리함 (/login)으로 접근 가능
    // 로그아웃 로직은 CustomLogoutFilter에서 처리함 (/logout)으로 접근 가능

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Validated SignupRequest signupRequest) throws Exception {
        // 이메일 인증 여부 확인
        if (!emailService.verifyCode(signupRequest.getEmail(), signupRequest.getVerificationCode())) {
            return new ResponseEntity<>("이메일 인증을 완료해주세요.", HttpStatus.BAD_REQUEST);
        }

        userService.signup(signupRequest);
        return new ResponseEntity<>("회원가입이 완료되었습니다.", HttpStatus.CREATED);
    }

    // 이메일 인증 번호 요청 API
    @PostMapping("/verify-email")
    public ResponseEntity<String> requestEmailVerification(@RequestBody EmailRequest email) {
        // 이메일로 인증 번호 발송
        emailService.sendVerificationEmail(email.getEmail());
        return new ResponseEntity<>("인증 번호가 이메일로 전송되었습니다.", HttpStatus.OK);
    }

    // 이메일 인증 확인 API
    @PostMapping("/verify-email/confirm")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequest request) {
        boolean isVerified = emailService.verifyCode(request.getEmail(), request.getVerificationCode());

        if (isVerified) {
            return new ResponseEntity<>("이메일 인증이 완료되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("인증 번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request) {
        User user = AuthFacade.getCurrentUser();
        if (user == null) {
            return new ResponseEntity<>("인증된 사용자가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            userService.updateProfile(user, request);
            return new ResponseEntity<>("프로필이 성공적으로 업데이트되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("프로필 업데이트 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
