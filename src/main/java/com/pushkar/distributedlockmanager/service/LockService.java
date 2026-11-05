package com.pushkar.distributedlockmanager.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class LockService {
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> releaseLockScript;
    private final DefaultRedisScript<Long> extendLockScript;

    private final Map<String, ScheduledFuture<?>> activeWatchdogs = new ConcurrentHashMap<>();

    private final ScheduledExecutorService watchdogExecutor = Executors.newScheduledThreadPool(2);

    @Autowired
    public LockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.releaseLockScript = new DefaultRedisScript<>();
        releaseLockScript.setLocation(new ClassPathResource("scripts/release_lock.lua"));
        releaseLockScript.setResultType(Long.class);

        this.extendLockScript = new DefaultRedisScript<>();
        extendLockScript.setLocation(new ClassPathResource("scripts/extend_lock.lua"));
        extendLockScript.setResultType(Long.class);
    }

    public boolean acquireLock(String lockKey, String lockValue, long ttlSeconds) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean success = ops.setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(success);
    }

    public boolean acquireLockWithWatchdog(String lockKey, String lockValue, long ttlSeconds) {
        boolean acquired = acquireLock(lockKey, lockValue, ttlSeconds);
        if(acquired) startWatchdog(lockKey, lockValue, ttlSeconds);
        return acquired;
    }

    private void startWatchdog(String lockKey, String lockValue, long ttlSeconds) {
        long intervalSeconds = Math.max(1, ttlSeconds / 3);

        ScheduledFuture<?> task = watchdogExecutor.scheduleAtFixedRate(() -> {
            boolean extended = extendLock(lockKey, lockValue, ttlSeconds);
            if(!extended) stopWatchdog(lockKey);
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        activeWatchdogs.put(lockKey, task);
    }

    private void stopWatchdog(String lockKey) {
        ScheduledFuture<?> task = activeWatchdogs.remove(lockKey);
        if (task != null) {
            task.cancel(false);
        }
    }

    public boolean extendLock(String lockKey, String lockValue, long ttlSeconds) {
        Long result = redisTemplate.execute(extendLockScript, Collections.singletonList(lockKey), lockValue, String.valueOf(ttlSeconds));
        return result != null && result == 1L;
    }

    public boolean releaseLock(String lockKey, String lockValue) {
        Long result = redisTemplate.execute(releaseLockScript, Collections.singletonList(lockKey), lockValue);
        return result != null && result == 1L;
    }

    @PreDestroy 
    public void shutdown() {
        watchdogExecutor.shutdownNow();
    }
}
