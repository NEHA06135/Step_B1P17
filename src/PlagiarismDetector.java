import java.util.*;

public class PlagiarismDetector {

    // n-gram size
    private static final int N = 5;

    // n-gram -> Set of document IDs
    private Map<String, Set<String>> ngramIndex;

    // documentId -> Set of its n-grams
    private Map<String, Set<String>> documentNgrams;

    public PlagiarismDetector() {
        ngramIndex = new HashMap<>();
        documentNgrams = new HashMap<>();
    }

    // Add document to database
    public void addDocument(String documentId, String content) {
        Set<String> ngrams = generateNgrams(content);
        documentNgrams.put(documentId, ngrams);

        for (String ngram : ngrams) {
            ngramIndex
                    .computeIfAbsent(ngram, k -> new HashSet<>())
                    .add(documentId);
        }
    }

    // Analyze document for plagiarism
    public void analyzeDocument(String documentId, String content) {

        Set<String> newDocNgrams = generateNgrams(content);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String ngram : newDocNgrams) {
            if (ngramIndex.containsKey(ngram)) {
                for (String docId : ngramIndex.get(ngram)) {
                    matchCount.put(docId,
                            matchCount.getOrDefault(docId, 0) + 1);
                }
            }
        }

        System.out.println("Extracted " + newDocNgrams.size() + " n-grams");

        String mostSimilarDoc = null;
        double highestSimilarity = 0;

        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String comparedDoc = entry.getKey();
            int matches = entry.getValue();

            int totalNgrams = documentNgrams.get(comparedDoc).size();
            double similarity = (matches * 100.0) / totalNgrams;

            System.out.println("→ Found " + matches +
                    " matching n-grams with \"" + comparedDoc + "\"");
            System.out.printf("→ Similarity: %.2f%%\n", similarity);

            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                mostSimilarDoc = comparedDoc;
            }
        }

        if (mostSimilarDoc != null) {
            System.out.println("\nMost Similar Document: " + mostSimilarDoc);
            if (highestSimilarity > 50) {
                System.out.println("PLAGIARISM DETECTED");
            }
        } else {
            System.out.println("No significant similarity found.");
        }
    }

    // Generate n-grams from content
    private Set<String> generateNgrams(String content) {
        Set<String> ngrams = new HashSet<>();

        String[] words = content
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }

        return ngrams;
    }

    // Main method for testing
    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        String essay1 = "Artificial intelligence is transforming the world "
                + "by enabling machines to learn from data and improve over time.";

        String essay2 = "Artificial intelligence is transforming the world "
                + "by enabling machines to learn from data and adapt quickly.";

        String essay3 = "The history of ancient civilizations is rich "
                + "with cultural development and innovation.";

        detector.addDocument("essay_089.txt", essay1);
        detector.addDocument("essay_092.txt", essay2);
        detector.addDocument("essay_050.txt", essay3);

        String newSubmission = "Artificial intelligence is transforming the world "
                + "by enabling machines to learn from data and improve over time.";

        detector.analyzeDocument("essay_123.txt", newSubmission);
    }
}