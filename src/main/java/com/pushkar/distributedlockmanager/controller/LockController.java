package com.pushkar.distributedlockmanager.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pushkar.distributedlockmanager.service.LockService;

@RestController
@RequestMapping("/lock")
public class LockController {
    private final LockService lockService;

    @Autowired
    public LockController(LockService lockService) {
        this.lockService = lockService;
    }

    @PostMapping("/acquire")
    public Map<String, Object> acquire(@RequestParam String key, @RequestParam(defaultValue = "10") long ttl) {
        String lockValue = UUID.randomUUID().toString();
        boolean acquired = lockService.acquireLock(key, lockValue, ttl);
        return Map.of("acquired", acquired, "key", key, "lockValue", lockValue);
    }

    @PostMapping("/release")
    public Map<String, Object> release(@RequestParam String key) {
        lockService.releaseLock(key);
        return Map.of("released", true, "key", key);
    }
}
