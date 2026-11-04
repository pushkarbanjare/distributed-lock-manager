package com.pushkar.distributedlockmanager.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class LockService {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public LockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean acquireLock(String lockKey, String lockValue, long ttlSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean success = ops.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
}
