package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

class LeadRateLimiterTest {

    @Test
    void shouldAllowFiveRequests() {
        LeadRateLimiter limiter = new LeadRateLimiter();

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow("192.0.2.1"));
        }
    }

    @Test
    void shouldRejectSixthRequest() {
        LeadRateLimiter limiter = new LeadRateLimiter();

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow("192.0.2.2"));
        }

        assertFalse(limiter.allow("192.0.2.2"));
    }

    @Test
    void shouldTrackIpsSeparately() {
        LeadRateLimiter limiter = new LeadRateLimiter();

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow("192.0.2.3"));
        }

        assertFalse(limiter.allow("192.0.2.3"));
        assertTrue(limiter.allow("192.0.2.4"));
    }

    @Test
    void shouldHandleConcurrentRequests() throws Exception {
        LeadRateLimiter limiter = new LeadRateLimiter();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return limiter.allow("192.0.2.5");
                }));
            }

            start.countDown();

            int allowed = 0;

            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    allowed++;
                }
            }

            assertEquals(5, allowed);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}