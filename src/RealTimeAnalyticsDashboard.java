import java.util.*;
import java.util.concurrent.*;

public class RealTimeAnalyticsDashboard {

    // pageUrl -> total visit count
    private ConcurrentHashMap<String, Integer> pageVisits;

    // pageUrl -> set of unique users
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors;

    // traffic source -> count
    private ConcurrentHashMap<String, Integer> sourceCounts;

    public RealTimeAnalyticsDashboard() {
        pageVisits = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        sourceCounts = new ConcurrentHashMap<>();
        startDashboardUpdater();
    }

    // Event class
    static class PageViewEvent {
        String url;
        String userId;
        String source;

        PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // Process incoming event (O(1))
    public void processEvent(PageViewEvent event) {

        // Update page visit count
        pageVisits.merge(event.url, 1, Integer::sum);

        // Update unique visitors
        uniqueVisitors
                .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                .add(event.userId);

        // Update traffic source count
        sourceCounts.merge(event.source, 1, Integer::sum);
    }

    // Get top 10 pages
    private List<Map.Entry<String, Integer>> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageVisits.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> b.getValue() - a.getValue());
        return result;
    }

    // Dashboard display
    public void getDashboard() {
        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : getTopPages()) {
            String url = entry.getKey();
            int visits = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();

            System.out.println(rank++ + ". " + url +
                    " - " + visits + " views (" + unique + " unique)");
        }

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : sourceCounts.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }

        System.out.println("================================\n");
    }

    // Auto refresh every 5 seconds
    private void startDashboardUpdater() {
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(this::getDashboard,
                5, 5, TimeUnit.SECONDS);
    }

    // Main method for testing
    public static void main(String[] args) throws InterruptedException {

        RealTimeAnalyticsDashboard dashboard =
                new RealTimeAnalyticsDashboard();

        // Simulate streaming events
        dashboard.processEvent(new PageViewEvent(
                "/article/breaking-news", "user_123", "google"));

        dashboard.processEvent(new PageViewEvent(
                "/article/breaking-news", "user_456", "facebook"));

        dashboard.processEvent(new PageViewEvent(
                "/sports/championship", "user_789", "direct"));

        dashboard.processEvent(new PageViewEvent(
                "/article/breaking-news", "user_123", "google"));

        dashboard.processEvent(new PageViewEvent(
                "/sports/championship", "user_999", "google"));

        // Keep program running
        Thread.sleep(20000);
    }
}