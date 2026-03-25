package com.mygame.server.util;

import com.mygame.server.domain.ports.ClockPort;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple sliding-window rate limiter per string key (e.g. player ID).
 * Rejects requests that exceed {@code maxPerSecond} in the past second.
 */
public final class RateLimiter {

    private final int       maxPerSecond;
    private final ClockPort clock;

    // entry = [windowStartMs, countInWindow]
    private final Map<String, long[]> windows = new ConcurrentHashMap<>();

    public RateLimiter(int maxPerSecond) {
        this(maxPerSecond, ClockPort.system());
    }

    public RateLimiter(int maxPerSecond, ClockPort clock) {
        this.maxPerSecond = maxPerSecond;
        this.clock        = clock;
    }

    /** @return true if the request is allowed, false if rate-limited */
    public boolean allow(String key) {
        long now   = clock.currentTimeMs();
        long[] win = windows.computeIfAbsent(key, k -> new long[]{now, 0L});
        synchronized (win) {
            if (now - win[0] >= 1000L) {
                win[0] = now;
                win[1] = 0;
            }
            if (win[1] >= maxPerSecond) return false;
            win[1]++;
        }
        return true;
    }

    public void remove(String key) {
        windows.remove(key);
    }
}
