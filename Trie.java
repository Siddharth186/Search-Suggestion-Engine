import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Trie is a prefix tree optimized for autocomplete search.
 * It supports insertion, prefix lookup, and word collection using DFS.
 */
public class Trie {
    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        String normalized = word.toLowerCase();
        TrieNode current = root;
        for (char character : normalized.toCharArray()) {
            current = current.getOrAddChild(character);
        }
        current.isEndOfWord = true;
        current.word = normalized;
        if (current.frequency == 0) {
            current.frequency = 1;
        }
    }

    public boolean contains(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        String normalized = word.toLowerCase();
        TrieNode current = root;
        for (char character : normalized.toCharArray()) {
            current = current.getChild(character);
            if (current == null) {
                return false;
            }
        }
        return current.isEndOfWord;
    }

    public TrieNode findPrefixNode(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return root;
        }
        String normalized = prefix.toLowerCase();
        TrieNode current = root;
        for (char character : normalized.toCharArray()) {
            current = current.getChild(character);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public List<String> collectWords(TrieNode node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<String> words = new ArrayList<>();
        collectWords(node, words);
        return words;
    }

    private void collectWords(TrieNode node, List<String> words) {
        if (node.isEndOfWord && node.word != null) {
            words.add(node.word);
        }
        Map<Character, TrieNode> sortedChildren = new TreeMap<>(node.children);
        for (TrieNode child : sortedChildren.values()) {
            collectWords(child, words);
        }
    }

    public void incrementFrequency(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        TrieNode current = findPrefixNode(word);
        if (current != null && current.isEndOfWord) {
            current.frequency++;
        }
    }

    public int size() {
        return countWords(root);
    }

    private int countWords(TrieNode node) {
        int count = node.isEndOfWord ? 1 : 0;
        for (TrieNode child : node.children.values()) {
            count += countWords(child);
        }
        return count;
    }
}
