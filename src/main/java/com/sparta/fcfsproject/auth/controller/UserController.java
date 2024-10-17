package com.sparta.fcfsproject.auth.controller;

import com.sparta.fcfsproject.auth.dto.LoginRequestDto;
import com.sparta.fcfsproject.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/")
    public String index(){
        return "success";
    }

    @PostMapping("/login")
    public String login(LoginRequestDto requestDto) {

        userService.login(requestDto);
        return "ok";
    }
    @GetMapping("/admin")
    public String adminP() {

        return "Admin Controller";
    }
}
