package com.pushkar.distributedlockmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class HealthController {
    
    @GetMapping("/health")
    public String healthCheck() {
        return "Distributed Lock Manager is up!";
    }
}
