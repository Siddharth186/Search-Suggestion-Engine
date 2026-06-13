import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DictionaryAPI fetches external autocomplete suggestions from Datamuse.
 */
public class DictionaryAPI {
    private static final String API_HOST = "api.datamuse.com";
    private static final String API_PATH = "/words";
    private static final int TIMEOUT_MS = 3000;
    private static final int MAX_SUGGESTIONS = 20;
    private final Map<String, List<String>> apiCache;

    public DictionaryAPI() {
        this.apiCache = new HashMap<>();
    }

    public List<String> fetchSuggestions(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>();
        }
        String normalized = prefix.trim().toLowerCase();
        if (apiCache.containsKey(normalized)) {
            return new ArrayList<>(apiCache.get(normalized));
        }

        List<String> suggestions = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(normalized + "*", StandardCharsets.UTF_8.name());
            String query = "sp=" + encoded + "&max=" + MAX_SUGGESTIONS;
            URI uri = new URI("https", API_HOST, API_PATH, query, null);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "GoogleStyleAutocomplete/1.0");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("API Error: HTTP " + status);
                return suggestions;
            }

            String payload = readResponse(connection);
            suggestions = parseJson(payload);
            apiCache.put(normalized, new ArrayList<>(suggestions));
        } catch (ConnectException e) {
            System.err.println("API Connection Error: " + e.getMessage());
        } catch (URISyntaxException e) {
            System.err.println("API Syntax Error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("API IO Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
        }
        return suggestions;
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private List<String> parseJson(String payload) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"word\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(payload);
        while (matcher.find()) {
            String word = matcher.group(1).toLowerCase();
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }
}
