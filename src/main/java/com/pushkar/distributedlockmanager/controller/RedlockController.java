package com.pushkar.distributedlockmanager.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.distributedlockmanager.service.RedlockService;

@RestController 
@RequestMapping("/redlock")
public class RedlockController {
    private final RedlockService redlockService;

    @Autowired 
    public RedlockController(RedlockService redlockService) {
        this.redlockService = redlockService;
    }

    @PostMapping("/acquire")
    public Map<String, Object> acquire(@RequestParam String key, @RequestParam(defaultValue = "10") long ttl) {
        String lockValue = UUID.randomUUID().toString();
        boolean acquired = redlockService.acquireLock(key, lockValue, ttl);
        return Map.of(
            "acquired", acquired,
            "key", key,
            "lockValue", lockValue
        );
    }

    @PostMapping("/release")
    public Map<String, Object> release(@RequestParam String key, @RequestParam String lockValue) {
        redlockService.releaseLock(key, lockValue);
        return Map.of("released", true, "key", key);
    }
}
