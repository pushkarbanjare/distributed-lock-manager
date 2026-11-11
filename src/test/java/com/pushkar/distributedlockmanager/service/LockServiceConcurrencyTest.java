package com.pushkar.distributedlockmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LockServiceConcurrencyTest {

    @Autowired
    private LockService lockService;

    @Test
    void onlyOneThreadShouldAcquireTheSameLock() throws InterruptedException {
        String lockKey = "test:concurrency:" + UUID.randomUUID();
        int threadCount = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLine = new CountDownLatch(1); // makes all threads wait, then fire together
        CountDownLatch finishLine = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startLine.await(); // all threads block here until released at once
                    boolean acquired = lockService.acquireLock(lockKey, UUID.randomUUID().toString(), 5);
                    if (acquired) successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLine.countDown();
                }
            }));
        }

        startLine.countDown(); // release all 20 threads at the exact same moment
        finishLine.await(10, TimeUnit.SECONDS); // wait for all to finish
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly ONE thread should have acquired the lock, not " + successCount.get());
    }
}
