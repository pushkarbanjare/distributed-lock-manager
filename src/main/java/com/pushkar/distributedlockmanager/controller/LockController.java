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
    public Map<String, Object> acquire(@RequestParam String key, @RequestParam(defaultValue = "10") long ttl, @RequestParam(defaultValue = "false") boolean autoExtend) {
        String lockValue = UUID.randomUUID().toString();
        boolean acquired = autoExtend ? lockService.acquireLockWithWatchdog(key, lockValue, ttl) : lockService.acquireLock(key, lockValue, ttl);
        return Map.of("acquired", acquired, "key", key, "lockValue", lockValue, "autoExtend", autoExtend);
    }

    @PostMapping("/release")
    public Map<String, Object> release(@RequestParam String key, @RequestParam String lockValue) {
        boolean released = lockService.releaseLock(key, lockValue);
        return Map.of("released", released, "key", key);
    }
}
