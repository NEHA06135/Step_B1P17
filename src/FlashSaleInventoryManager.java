import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {

    // productId -> stock count (Atomic for thread safety)
    private ConcurrentHashMap<String, AtomicInteger> inventory;

    // productId -> waiting list (FIFO)
    private ConcurrentHashMap<String, Queue<Integer>> waitingList;

    public FlashSaleInventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Add product with initial stock
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Instant stock check (O(1))
    public String checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);
        if (stock == null) {
            return "Product not found";
        }
        return stock.get() + " units available";
    }

    // Purchase item (Thread-safe, O(1))
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = inventory.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {
            int currentStock = stock.get();

            if (currentStock > 0) {
                // Atomic decrement
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            } else {
                // Add to waiting list
                Queue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Added to waiting list, position #" + queue.size();
            }
        }
    }

    // View waiting list position
    public int getWaitingListSize(String productId) {
        Queue<Integer> queue = waitingList.get(productId);
        return queue == null ? 0 : queue.size();
    }

    // Main method for testing
    public static void main(String[] args) {

        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println(manager.checkStock("IPHONE15_256GB"));

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));

        // Simulate selling all units
        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", 1000 + i);
        }

        // Now stock should be 0
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));
    }
}