import java.util.*;

public class AutocompleteSystem {

    // Store global frequency
    private Map<String, Integer> frequencyMap;

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }

    private TrieNode root;

    public AutocompleteSystem() {
        frequencyMap = new HashMap<>();
        root = new TrieNode();
    }

    // Insert query into Trie
    private void insertIntoTrie(String query) {
        TrieNode current = root;
        for (char c : query.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    // Add or update frequency
    public void updateFrequency(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
        insertIntoTrie(query);
    }

    // Search prefix
    public List<String> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        List<String> results = new ArrayList<>();
        dfs(node, new StringBuilder(prefix), results);

        // Get Top 10 using Min-Heap
        PriorityQueue<String> minHeap =
                new PriorityQueue<>((a, b) ->
                        frequencyMap.get(a) - frequencyMap.get(b));

        for (String result : results) {
            minHeap.offer(result);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<String> topResults = new ArrayList<>(minHeap);
        topResults.sort((a, b) ->
                frequencyMap.get(b) - frequencyMap.get(a));

        return topResults;
    }

    // DFS to collect words under prefix
    private void dfs(TrieNode node, StringBuilder path, List<String> results) {
        if (node.isEndOfWord) {
            results.add(path.toString());
        }

        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            path.append(entry.getKey());
            dfs(entry.getValue(), path, results);
            path.deleteCharAt(path.length() - 1);
        }
    }

    // Basic typo suggestion (edit distance 1: remove last char)
    public List<String> suggestWithTypo(String input) {
        List<String> results = search(input);

        if (!results.isEmpty()) return results;

        if (input.length() > 1) {
            return search(input.substring(0, input.length() - 1));
        }

        return Collections.emptyList();
    }

    // Main for testing
    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        system.updateFrequency("java tutorial");
        system.updateFrequency("javascript");
        system.updateFrequency("java download");
        system.updateFrequency("java tutorial");
        system.updateFrequency("java tutorial");
        system.updateFrequency("java 21 features");

        System.out.println("Search results for 'jav':");
        List<String> results = system.search("jav");

        int rank = 1;
        for (String res : results) {
            System.out.println(rank++ + ". " + res +
                    " (" + system.frequencyMap.get(res) + " searches)");
        }

        System.out.println("\nUpdating frequency for 'java 21 features'");
        system.updateFrequency("java 21 features");

        System.out.println("New frequency: "
                + system.frequencyMap.get("java 21 features"));
    }
}