package com.pushkar.distributedlockmanager.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service 
public class RedlockService {
    private final List<StringRedisTemplate> nodes;
    private final DefaultRedisScript<Long> releaseLockScript;

    @Autowired 
    public RedlockService(List<StringRedisTemplate> redlockTemplates) {
        this.nodes = redlockTemplates;
        this.releaseLockScript = new DefaultRedisScript<>();
        releaseLockScript.setLocation(new ClassPathResource("scripts/release_lock.lua"));
        releaseLockScript.setResultType(Long.class);
    }

    public boolean acquireLock(String lockKey, String lockValue, long ttlSeconds) {
        int quorum = (nodes.size() / 2) + 1;
        int successCount = 0;

        for (StringRedisTemplate node : nodes) {
            try {
                Boolean success = node.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds));
                if (Boolean.TRUE.equals(success)) successCount++;
            } catch (Exception e) {
                System.out.println("Node unreachable, skipping: " + e.getMessage());
            }
        }

        if(successCount >= quorum) return true;
        else {
            releaseLock(lockKey, lockValue);
            return false;
        }
    }

    public void releaseLock(String lockKey, String lockValue) {
        for(StringRedisTemplate node: nodes) {
            try {
                node.execute(releaseLockScript, Collections.singletonList(lockKey), lockValue);
            }
            catch(Exception e) {
                System.out.println("Node unreachable during release, skipping: " + e.getMessage());
            }
        }
    }
}
