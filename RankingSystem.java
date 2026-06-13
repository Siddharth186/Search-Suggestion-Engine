import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RankingSystem computes a combined score for suggestions using frequency
 * and recency. It also tracks recent search history for boost calculation.
 */
public class RankingSystem {
    private final Map<String, Integer> frequencyMap;
    private final Map<String, Long> lastSearchTime;
    private final Deque<String> recentQueries;
    private final int historyLimit;

    public RankingSystem(int historyLimit) {
        this.frequencyMap = new HashMap<>();
        this.lastSearchTime = new HashMap<>();
        this.recentQueries = new ArrayDeque<>();
        this.historyLimit = historyLimit;
    }

    public RankingSystem() {
        this(100);
    }

    public static class Suggestion {
        public final String word;
        public final int frequency;
        public final int recencyWeight;
        public final int score;

        public Suggestion(String word, int frequency, int recencyWeight, int score) {
            this.word = word;
            this.frequency = frequency;
            this.recencyWeight = recencyWeight;
            this.score = score;
        }

        @Override
        public String toString() {
            return word + " (score:" + score + ")";
        }
    }

    public void registerWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        frequencyMap.putIfAbsent(word, 1);
    }

    public void recordSearch(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        int frequency = frequencyMap.getOrDefault(word, 1) + 1;
        frequencyMap.put(word, frequency);
        long timestamp = System.currentTimeMillis();
        lastSearchTime.put(word, timestamp);
        addRecentQuery(word);
    }

    public Suggestion score(String word) {
        int frequency = frequencyMap.getOrDefault(word, 1);
        int recencyWeight = calculateRecencyWeight(word);
        int score = frequency + recencyWeight;
        return new Suggestion(word, frequency, recencyWeight, score);
    }

    public List<Suggestion> scoreWords(Collection<String> words) {
        List<Suggestion> suggestions = new ArrayList<>();
        for (String word : words) {
            suggestions.add(score(word));
        }
        return suggestions;
    }

    public List<String> getRecentQueries() {
        return new ArrayList<>(recentQueries);
    }

    public int getFrequency(String word) {
        return frequencyMap.getOrDefault(word, 1);
    }

    private int calculateRecencyWeight(String word) {
        Long lastUsed = lastSearchTime.get(word);
        if (lastUsed == null) {
            return 0;
        }
        long ageMinutes = (System.currentTimeMillis() - lastUsed) / 60000;
        int weight = Math.max(0, 50 - (int) ageMinutes);
        return weight;
    }

    private void addRecentQuery(String word) {
        if (recentQueries.contains(word)) {
            recentQueries.remove(word);
        }
        recentQueries.addFirst(word);
        while (recentQueries.size() > historyLimit) {
            recentQueries.removeLast();
        }
    }
}
