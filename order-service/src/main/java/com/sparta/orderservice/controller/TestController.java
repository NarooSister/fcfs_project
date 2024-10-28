package com.sparta.orderservice.controller;

import com.sparta.orderservice.service.ResilientCallerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private final ResilientCallerService resilientCallerService;

    public TestController(ResilientCallerService resilientCallerService) {
        this.resilientCallerService = resilientCallerService;
    }

    @GetMapping("/test")
    public String test() {
        return resilientCallerService.callCase1WithResilience();
    }
}
