import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class FinancialTransactionAnalyzer {

    // Transaction class
    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        LocalDateTime time;

        public Transaction(int id, double amount,
                           String merchant,
                           String account,
                           LocalDateTime time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = time;
        }

        public String toString() {
            return "id:" + id + " amount:" + amount;
        }
    }

    private List<Transaction> transactions;

    public FinancialTransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // 1️⃣ Classic Two-Sum
    public List<List<Transaction>> findTwoSum(double target) {

        Map<Double, Transaction> map = new HashMap<>();
        List<List<Transaction>> result = new ArrayList<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add(Arrays.asList(map.get(complement), t));
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // 2️⃣ Two-Sum within 1-hour window
    public List<List<Transaction>> findTwoSumWithTimeWindow(double target) {

        List<List<Transaction>> result = new ArrayList<>();
        Map<Double, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {

            double complement = target - t.amount;

            if (map.containsKey(complement)) {
                for (Transaction prev : map.get(complement)) {

                    long minutes = Math.abs(
                            ChronoUnit.MINUTES.between(prev.time, t.time));

                    if (minutes <= 60) {
                        result.add(Arrays.asList(prev, t));
                    }
                }
            }

            map.computeIfAbsent(t.amount,
                    k -> new ArrayList<>()).add(t);
        }

        return result;
    }

    // 3️⃣ K-Sum (recursive)
    public List<List<Transaction>> findKSum(int k, double target) {
        List<List<Transaction>> result = new ArrayList<>();
        kSumHelper(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(int start, int k,
                            double target,
                            List<Transaction> current,
                            List<List<Transaction>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        if (k == 0 || start >= transactions.size())
            return;

        for (int i = start; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);

            current.add(t);
            kSumHelper(i + 1,
                    k - 1,
                    target - t.amount,
                    current,
                    result);
            current.remove(current.size() - 1);
        }
    }

    // 4️⃣ Duplicate Detection
    public Map<String, List<Transaction>> detectDuplicates() {

        Map<String, List<Transaction>> duplicateMap = new HashMap<>();

        for (Transaction t : transactions) {

            String key = t.amount + "|" + t.merchant;

            duplicateMap
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(t);
        }

        // Filter only duplicates (more than 1 account)
        Map<String, List<Transaction>> result = new HashMap<>();

        for (Map.Entry<String, List<Transaction>> entry :
                duplicateMap.entrySet()) {

            Set<String> accounts = new HashSet<>();

            for (Transaction t : entry.getValue()) {
                accounts.add(t.account);
            }

            if (accounts.size() > 1) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    // Main method
    public static void main(String[] args) {

        List<Transaction> txns = Arrays.asList(
                new Transaction(1, 500, "Store A",
                        "acc1", LocalDateTime.of(2026,2,27,10,0)),
                new Transaction(2, 300, "Store B",
                        "acc2", LocalDateTime.of(2026,2,27,10,15)),
                new Transaction(3, 200, "Store C",
                        "acc3", LocalDateTime.of(2026,2,27,10,30)),
                new Transaction(4, 500, "Store A",
                        "acc4", LocalDateTime.of(2026,2,27,11,0))
        );

        FinancialTransactionAnalyzer analyzer =
                new FinancialTransactionAnalyzer(txns);

        System.out.println("Two-Sum:");
        System.out.println(analyzer.findTwoSum(500));

        System.out.println("\nTwo-Sum within 1 hour:");
        System.out.println(analyzer.findTwoSumWithTimeWindow(500));

        System.out.println("\nK-Sum (k=3, target=1000):");
        System.out.println(analyzer.findKSum(3, 1000));

        System.out.println("\nDuplicate Detection:");
        System.out.println(analyzer.detectDuplicates());
    }
}