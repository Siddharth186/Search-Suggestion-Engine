import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CacheManager stores recent query results using a fixed-size LRU cache.
 * This avoids repeated Trie and API work for hot prefixes.
 */
public class CacheManager {
    private final Map<String, List<RankingSystem.Suggestion>> cache;

    public CacheManager(int maxEntries) {
        this.cache = new LinkedHashMap<String, List<RankingSystem.Suggestion>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<RankingSystem.Suggestion>> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public List<RankingSystem.Suggestion> get(String key) {
        List<RankingSystem.Suggestion> results = cache.get(key);
        if (results == null) {
            return null;
        }
        return new ArrayList<>(results);
    }

    public void put(String key, List<RankingSystem.Suggestion> results) {
        cache.put(key, new ArrayList<>(results));
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }
}
