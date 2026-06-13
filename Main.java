import java.util.List;
import java.util.Scanner;

/**
 * Main is the interactive CLI entry point for the autocomplete system.
 */
public class Main {
    private static final String DICTIONARY_FILE = "words.txt";
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final SearchEngine ENGINE = new SearchEngine();

    public static void main(String[] args) {
        printHeader();
        ENGINE.loadDictionary(DICTIONARY_FILE);
        runMenu();
        SCANNER.close();
        System.out.println("\n👋 Goodbye!");
    }

    private static void printHeader() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║          Google-Style Search Autocomplete System         ║");
        System.out.println("║          Trie + Cache + API + Smart Ranking             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
    }

    private static void runMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n1. Search");
            System.out.println("2. Recent queries");
            System.out.println("3. Cache status");
            System.out.println("4. Run test suite");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = SCANNER.nextLine().trim();
            switch (choice) {
                case "1":
                    searchMode();
                    break;
                case "2":
                    showRecentQueries();
                    break;
                case "3":
                    showCacheStatus();
                    break;
                case "4":
                    runTestSuite();
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void searchMode() {
        System.out.println("\nEnter a prefix to search. Type 'back' to return.");
        while (true) {
            System.out.print("Prefix: ");
            String prefix = SCANNER.nextLine().trim();
            if (prefix.equalsIgnoreCase("back")) {
                break;
            }
            if (prefix.isEmpty()) {
                System.out.println("Please enter a valid prefix.");
                continue;
            }
            long start = System.currentTimeMillis();
            List<RankingSystem.Suggestion> suggestions = ENGINE.search(prefix);
            long elapsed = System.currentTimeMillis() - start;
            displaySuggestions(prefix, suggestions, elapsed);
        }
    }

    private static void displaySuggestions(String prefix, List<RankingSystem.Suggestion> suggestions, long elapsed) {
        System.out.println("\nTop " + suggestions.size() + " suggestions for '" + prefix + "':");
        for (int i = 0; i < suggestions.size(); i++) {
            RankingSystem.Suggestion suggestion = suggestions.get(i);
            System.out.printf("%2d. %-30s score=%3d frequency=%3d recency=%3d\n",
                    i + 1,
                    suggestion.word,
                    suggestion.score,
                    suggestion.frequency,
                    suggestion.recencyWeight);
        }
        System.out.println("Computed in " + elapsed + "ms");
    }

    private static void showRecentQueries() {
        System.out.println("\nRecent queries:");
        for (String query : ENGINE.getRecentQueries()) {
            System.out.println("- " + query);
        }
    }

    private static void showCacheStatus() {
        System.out.println("\nCache size: " + ENGINE.getCacheSize() + " entries.");
    }

    private static void runTestSuite() {
        String[] testPrefixes = {"app", "go", "the", "java", "prog", "sp", "x", "auto"};
        System.out.println("\nRunning test suite...");
        for (String prefix : testPrefixes) {
            long start = System.currentTimeMillis();
            List<RankingSystem.Suggestion> suggestions = ENGINE.search(prefix);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("\nPrefix: " + prefix + " (" + suggestions.size() + " results, " + elapsed + "ms)");
            for (int i = 0; i < Math.min(5, suggestions.size()); i++) {
                RankingSystem.Suggestion suggestion = suggestions.get(i);
                System.out.printf("  %d. %s (score=%d)\n", i + 1, suggestion.word, suggestion.score);
            }
        }
    }
}
