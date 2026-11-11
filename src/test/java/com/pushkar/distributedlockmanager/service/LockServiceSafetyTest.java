package com.pushkar.distributedlockmanager.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest 
public class LockServiceSafetyTest {
    
    @Autowired 
    private LockService lockService;

    @Test 
    void shouldNotReleaseLockOwnedBySomeoneElse() {
        String lockKey = "test:safety:" + UUID.randomUUID();
        String realOwnerValue = UUID.randomUUID().toString();
        String impostorValue = UUID.randomUUID().toString();

        boolean acquired = lockService.acquireLock(lockKey, realOwnerValue, 10);
        assertTrue(acquired, "Should acquire the lock initially");

        boolean releasedByImpostor = lockService.releaseLock(lockKey, impostorValue);
        assertFalse(releasedByImpostor, "A non-owner must NOT be able to release the lock");

        boolean releasedByRealOwner = lockService.releaseLock(lockKey, realOwnerValue);
        assertTrue(releasedByRealOwner, "The real owner should be able to release it");
    }
}
