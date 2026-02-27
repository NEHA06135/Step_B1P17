import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DistributedRateLimiter {

    // clientId -> TokenBucket
    private ConcurrentHashMap<String, TokenBucket> clientBuckets;

    private static final long MAX_TOKENS = 1000;              // 1000 requests
    private static final long REFILL_DURATION_MS = 3600_000;  // 1 hour
    private static final double REFILL_RATE =
            (double) MAX_TOKENS / REFILL_DURATION_MS;         // tokens per ms

    public DistributedRateLimiter() {
        clientBuckets = new ConcurrentHashMap<>();
    }

    // Token Bucket Class
    static class TokenBucket {
        private double tokens;
        private long lastRefillTime;

        public TokenBucket() {
            this.tokens = MAX_TOKENS;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // Refill tokens based on time passed
        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;

            double tokensToAdd = timePassed * REFILL_RATE;
            tokens = Math.min(MAX_TOKENS, tokens + tokensToAdd);

            lastRefillTime = now;
        }

        // Try consuming 1 token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        public synchronized long getRemainingTokens() {
            refill();
            return (long) tokens;
        }

        public synchronized long getResetTimeSeconds() {
            refill();
            if (tokens >= MAX_TOKENS) return 0;

            double tokensNeeded = MAX_TOKENS - tokens;
            long msToFull = (long) (tokensNeeded / REFILL_RATE);
            return msToFull / 1000;
        }
    }

    // Check rate limit
    public String checkRateLimit(String clientId) {

        TokenBucket bucket = clientBuckets
                .computeIfAbsent(clientId, k -> new TokenBucket());

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens()
                    + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after "
                    + bucket.getResetTimeSeconds() + "s)";
        }
    }

    // Get status
    public String getRateLimitStatus(String clientId) {

        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket == null) {
            return "{used: 0, limit: " + MAX_TOKENS +
                    ", reset: 0}";
        }

        long remaining = bucket.getRemainingTokens();
        long used = MAX_TOKENS - remaining;

        return "{used: " + used +
                ", limit: " + MAX_TOKENS +
                ", reset: " + bucket.getResetTimeSeconds() + "}";
    }

    // Main method for testing
    public static void main(String[] args) {

        DistributedRateLimiter limiter = new DistributedRateLimiter();
        String clientId = "abc123";

        // Simulate some requests
        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(clientId));
        }

        System.out.println(limiter.getRateLimitStatus(clientId));
    }
}
