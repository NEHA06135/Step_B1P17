import java.util.*;
import java.util.concurrent.*;

public class DNSCache {

    // DNS Entry Class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int maxSize;
    private final Map<String, DNSEntry> cache;
    private long hits = 0;
    private long misses = 0;
    private long totalLookupTime = 0;

    // Constructor with LRU support
    public DNSCache(int maxSize) {
        this.maxSize = maxSize;

        this.cache = Collections.synchronizedMap(
                new LinkedHashMap<String, DNSEntry>(maxSize, 0.75f, true) {
                    protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                        return size() > DNSCache.this.maxSize;
                    }
                }
        );

        startCleanupThread();
    }

    // Resolve domain
    public String resolve(String domain) {
        long startTime = System.nanoTime();

        synchronized (cache) {
            DNSEntry entry = cache.get(domain);

            if (entry != null) {
                if (!entry.isExpired()) {
                    hits++;
                    totalLookupTime += (System.nanoTime() - startTime);
                    return "Cache HIT → " + entry.ipAddress;
                } else {
                    cache.remove(domain);
                }
            }

            // Cache MISS
            misses++;
            String ip = queryUpstreamDNS(domain);
            cache.put(domain, new DNSEntry(domain, ip, 300));

            totalLookupTime += (System.nanoTime() - startTime);
            return "Cache MISS → Queried upstream → " + ip;
        }
    }

    // Simulated upstream DNS query
    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // Simulate 100ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Fake IP generation
        return "172.217.14." + new Random().nextInt(255);
    }

    // Background thread to clean expired entries
    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Clean every 5 seconds
                    synchronized (cache) {
                        Iterator<Map.Entry<String, DNSEntry>> iterator = cache.entrySet().iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().getValue().isExpired()) {
                                iterator.remove();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }

    // Cache statistics
    public String getCacheStats() {
        long totalRequests = hits + misses;
        double hitRate = totalRequests == 0 ? 0 :
                ((double) hits / totalRequests) * 100;

        double avgLookupMs = totalRequests == 0 ? 0 :
                (totalLookupTime / 1_000_000.0) / totalRequests;

        return String.format("Hit Rate: %.2f%%, Avg Lookup Time: %.2f ms",
                hitRate, avgLookupMs);
    }

    // Main method for testing
    public static void main(String[] args) {

        DNSCache dnsCache = new DNSCache(5);

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("openai.com"));
        System.out.println(dnsCache.resolve("google.com"));

        System.out.println(dnsCache.getCacheStats());
    }
}