public class UsernameAvailabilityChecker
{
    import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

    public class UsernameAvailabilityChecker {

        // Stores registered usernames (username -> userId)
        private ConcurrentHashMap<String, Integer> usernameMap;

        // Stores attempt frequency (username -> attempt count)
        private ConcurrentHashMap<String, Integer> attemptCount;

        public UsernameAvailabilityChecker() {
            usernameMap = new ConcurrentHashMap<>();
            attemptCount = new ConcurrentHashMap<>();
        }

        // Check username availability in O(1)
        public boolean checkAvailability(String username) {
            attemptCount.put(username, attemptCount.getOrDefault(username, 0) + 1);
            return !usernameMap.containsKey(username);
        }

        // Register a new user
        public void registerUser(String username, int userId) {
            usernameMap.put(username, userId);
        }

        // Suggest similar available usernames
        public List<String> suggestAlternatives(String username) {
            List<String> suggestions = new ArrayList<>();

            // Append numbers
            for (int i = 1; i <= 5; i++) {
                String newUsername = username + i;
                if (!usernameMap.containsKey(newUsername)) {
                    suggestions.add(newUsername);
                }
            }

            // Replace underscore with dot
            if (username.contains("_")) {
                String modified = username.replace("_", ".");
                if (!usernameMap.containsKey(modified)) {
                    suggestions.add(modified);
                }
            }

            return suggestions;
        }

        // Get most attempted username
        public String getMostAttempted() {
            String mostAttempted = "";
            int maxCount = 0;

            for (Map.Entry<String, Integer> entry : attemptCount.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostAttempted = entry.getKey();
                }
            }

            return mostAttempted + " (" + maxCount + " attempts)";
        }

        // Main method for testing
        public static void main(String[] args) {
            UsernameAvailabilityChecker checker = new UsernameAvailabilityChecker();

            // Register some users
            checker.registerUser("john_doe", 101);
            checker.registerUser("admin", 1);

            System.out.println("checkAvailability('john_doe'): "
                    + checker.checkAvailability("john_doe"));

            System.out.println("checkAvailability('jane_smith'): "
                    + checker.checkAvailability("jane_smith"));

            System.out.println("suggestAlternatives('john_doe'): "
                    + checker.suggestAlternatives("john_doe"));

            // Simulate multiple attempts
            checker.checkAvailability("admin");
            checker.checkAvailability("admin");
            checker.checkAvailability("admin");

            System.out.println("getMostAttempted(): "
                    + checker.getMostAttempted());
        }
    }
}
