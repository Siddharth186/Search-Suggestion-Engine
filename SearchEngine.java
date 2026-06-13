import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * SearchEngine coordinates dictionary loading, Trie search, cache lookup,
 * API fallback, and ranking.
 */
public class SearchEngine {
    private static final int MAX_SUGGESTIONS = 10;
    private static final int API_FALLBACK_THRESHOLD = 3;

    private final Trie trie;
    private final CacheManager cacheManager;
    private final RankingSystem rankingSystem;
    private final DictionaryAPI dictionaryAPI;

    public SearchEngine() {
        this.trie = new Trie();
        this.cacheManager = new CacheManager(250);
        this.rankingSystem = new RankingSystem(100);
        this.dictionaryAPI = new DictionaryAPI();
    }

    public void loadDictionary(String dictionaryPath) {
        System.out.println("📚 Loading dictionary from " + dictionaryPath + "...");
        List<String> words = DictionaryLoader.loadWords(dictionaryPath);
        for (String word : words) {
            trie.insert(word);
            rankingSystem.registerWord(word);
        }
        System.out.println("✓ Loaded " + trie.size() + " words into Trie");
    }

    public List<RankingSystem.Suggestion> search(String prefix) {
        String normalized = normalize(prefix);
        if (normalized.isEmpty()) {
            return new ArrayList<>();
        }

        if (cacheManager.contains(normalized)) {
            return cacheManager.get(normalized);
        }

        List<String> candidates = new ArrayList<>();
        TrieNode node = trie.findPrefixNode(normalized);
        if (node != null) {
            candidates.addAll(trie.collectWords(node));
        }

        if (candidates.size() < API_FALLBACK_THRESHOLD) {
            List<String> apiWords = dictionaryAPI.fetchSuggestions(normalized);
            Set<String> unique = new HashSet<>(candidates);
            for (String word : apiWords) {
                if (unique.add(word)) {
                    candidates.add(word);
                }
            }
        }

        List<RankingSystem.Suggestion> ranked = rankCandidates(candidates);
        cacheManager.put(normalized, ranked);
        if (trie.contains(normalized)) {
            rankingSystem.recordSearch(normalized);
        }
        return ranked;
    }

    public List<String> getRecentQueries() {
        return rankingSystem.getRecentQueries();
    }

    public int getCacheSize() {
        return cacheManager.size();
    }

    private List<RankingSystem.Suggestion> rankCandidates(List<String> candidates) {
        PriorityQueue<RankingSystem.Suggestion> heap = new PriorityQueue<>(MAX_SUGGESTIONS,
                (a, b) -> a.score - b.score);
        Set<String> seen = new HashSet<>();

        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            String normalized = normalize(candidate);
            if (!seen.add(normalized)) {
                continue;
            }
            RankingSystem.Suggestion suggestion = rankingSystem.score(normalized);
            if (heap.size() < MAX_SUGGESTIONS) {
                heap.offer(suggestion);
            } else if (suggestion.score > heap.peek().score) {
                heap.poll();
                heap.offer(suggestion);
            }
        }

        List<RankingSystem.Suggestion> top = new ArrayList<>(heap);
        top.sort((a, b) -> {
            int scoreDiff = b.score - a.score;
            return scoreDiff != 0 ? scoreDiff : a.word.compareTo(b.word);
        });
        return top;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
