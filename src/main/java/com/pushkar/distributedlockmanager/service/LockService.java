package com.pushkar.distributedlockmanager.service;

import java.time.Duration;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class LockService {
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> releaseLockScript;

    @Autowired
    public LockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.releaseLockScript = new DefaultRedisScript<>();
        releaseLockScript.setLocation(new ClassPathResource("scripts/release_lock.lua"));
        releaseLockScript.setResultType(Long.class);
    }

    public boolean acquireLock(String lockKey, String lockValue, long ttlSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean success = ops.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(success);
    }

    public boolean releaseLock(String lockKey, String lockValue) {
        Long result = redisTemplate.execute(releaseLockScript, Collections.singletonList(lockKey), lockValue);
        return result != null && result == 1L;
    }
}
