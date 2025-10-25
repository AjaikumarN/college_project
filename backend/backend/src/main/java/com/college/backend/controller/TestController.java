package com.college.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
            "status", "success",
            "message", "College ERP Backend is running!",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "application", "College ERP Backend"
        );
    }
}