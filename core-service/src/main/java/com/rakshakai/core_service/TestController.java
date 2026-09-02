package com.rakshakai.core_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String healthCheck() {
        return "Project RAKSHAK-AI Core Service is running!";
    }
}