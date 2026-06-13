import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DictionaryLoader loads dictionary words from a file.
 * It is optimized for large dictionaries and provides a fallback word list
 * if the file is unavailable.
 */
public class DictionaryLoader {
    private static final List<String> FALLBACK_WORDS = Arrays.asList(
        "app", "apple", "application", "apply", "approve", "appointment",
        "approval", "apricot", "april", "approximate",
        "google", "go", "good", "gmail", "google chrome", "google maps",
        "the", "that", "this", "they", "them", "these", "then", "there",
        "three", "through", "throw", "thumb", "thunder", "java", "javascript",
        "python", "programming", "programmer", "search", "searching", "searcher",
        "spotify", "special", "speed", "spell", "spider", "spring", "star", "start"
    );

    /**
     * Load words from the provided dictionary path.
     * @param dictionaryPath File path to the dictionary file.
     * @return List of loaded words (lowercased).
     */
    public static List<String> loadWords(String dictionaryPath) {
        Path path = Paths.get(dictionaryPath);
        if (Files.exists(path)) {
            return loadFromFile(path);
        }
        System.out.println("⚠ Dictionary file not found at " + dictionaryPath + ". Using fallback words.");
        return new ArrayList<>(FALLBACK_WORDS);
    }

    private static List<String> loadFromFile(Path path) {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = normalize(line);
                if (!normalized.isEmpty()) {
                    words.add(normalized);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading dictionary file: " + e.getMessage());
            return new ArrayList<>(FALLBACK_WORDS);
        }
        return words;
    }

    private static String normalize(String word) {
        return word == null ? "" : word.trim().toLowerCase();
    }
}
