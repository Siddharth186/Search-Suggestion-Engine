import java.util.HashMap;
import java.util.Map;

/**
 * TrieNode represents a single node in the Trie prefix tree.
 * It stores children, an end-of-word marker, the full word, and a frequency count.
 */
public class TrieNode {
    public final Map<Character, TrieNode> children;
    public boolean isEndOfWord;
    public String word;
    public int frequency;

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.word = null;
        this.frequency = 0;
    }

    public TrieNode getChild(char character) {
        return children.get(character);
    }

    public TrieNode getOrAddChild(char character) {
        return children.computeIfAbsent(character, key -> new TrieNode());
    }
}
