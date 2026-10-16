package vn.edu.ictu.qlkh.service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class LeadRateLimiter {

    private static final int LIMIT = 5;
    private static final int MAX_IPS = 10000;
    private static final long WINDOW_MS = 60000L;

    private final Map<String, ArrayDeque<Long>> requests = new HashMap<>();

    public synchronized boolean allow(String ip) {
        long now = System.currentTimeMillis();
        String key = ip == null ? "unknown" : ip;

        cleanup(now);

        ArrayDeque<Long> attempts = requests.get(key);

        if (attempts == null) {
            if (requests.size() >= MAX_IPS) {
                return false;
            }

            attempts = new ArrayDeque<>();
            requests.put(key, attempts);
        }

        if (attempts.size() >= LIMIT) {
            return false;
        }

        attempts.addLast(now);
        return true;
    }

    private void cleanup(long now) {
        Iterator<Map.Entry<String, ArrayDeque<Long>>> iterator =
                requests.entrySet().iterator();

        while (iterator.hasNext()) {
            ArrayDeque<Long> attempts = iterator.next().getValue();

            while (!attempts.isEmpty()
                    && now - attempts.peekFirst() >= WINDOW_MS) {
                attempts.removeFirst();
            }

            if (attempts.isEmpty()) {
                iterator.remove();
            }
        }
    }
}