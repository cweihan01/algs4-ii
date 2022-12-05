import java.util.Iterator;

import edu.princeton.cs.algs4.Queue;

/**
 * TrieSet26 class.
 * Contains 26 buckets for A-Z.
 * Adapted from algs4 TrieSET data structure, included 'containsPrefix' method
 * and removed irrelevant methods for this project.
 */
public class TrieSet26 implements Iterable<String> {
    private static final int R = 26;
    private Node root;

    /**
     * Private Node class. Each Node has 26 buckets for A-Z, and isString is true
     * if the word formed from the root to the current Node is inside the
     * dictionary.
     */
    private static class Node {
        private Node[] next = new Node[R];
        private boolean isString;
    }

    /**
     * Constructor initializes empty TrieSet.
     */
    public TrieSet26() {
    }

    /**
     * Adds a key to the trie.
     * 
     * @param key
     */
    public void add(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("argument key is null");
        }
        root = this.add(key, root, 0);
    }

    /**
     * Helper method to add a key recursively.
     * 
     * @param key
     * @param node - current node
     * @param idx  - index of key we are currently adding
     * @return the next Node which has been already added (add from end of key)
     */
    private Node add(String key, Node node, int idx) {
        if (node == null) {
            node = new Node();
        }

        if (idx == key.length()) {
            // reach end of key, add it to trie
            node.isString = true;
        } else {
            // recursively add next char in key
            int c = this.charAt(key, idx);
            node.next[c] = this.add(key, node.next[c], idx + 1);
        }
        return node;
    }

    /**
     * Checks if the trie contains a given key.
     * 
     * @param key
     * @return true if key in trie
     */
    public boolean contains(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("argument key is null");
        }

        Node node = this.get(key, root, 0);
        if (node == null) {
            return false;
        }
        return node.isString;
    }

    /**
     * Checks if trie contains the given prefix.
     * 
     * @param prefix
     * @return true if prefix in trie
     */
    public boolean containsPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("argument prefix is null");
        }
        return this.get(prefix, root, 0) != null;
    }

    /**
     * Helper method to retrieve a Node from trie given a key.
     * 
     * @param key
     * @param node
     * @param idx
     * @return Node that is retrieved by tracing each char in key, null if no such
     *         Node exists
     */
    private Node get(String key, Node node, int idx) {
        if (node == null) {
            return null;
        }
        if (idx == key.length()) {
            return node;
        }
        return this.get(key, node.next[this.charAt(key, idx)], idx + 1);
    }

    /**
     * Helper method that converts the ASCII representation of A-Z (65-90) to its
     * corresponding zero-indexed form to index into Node next buckets.
     * 
     * @param key
     * @param idx
     * @return int representing the zero-indexed ASCII of the char at given index
     */
    private int charAt(String key, int idx) {
        return key.charAt(idx) - 'A';
    }

    /**
     * Trie iterator.
     * 
     * @return all words in Trie in sequential order
     */
    @Override
    public Iterator<String> iterator() {
        Queue<String> results = new Queue<>();
        this.collect(root, new StringBuilder(), results);
        return results.iterator();
    }

    /**
     * Helper function to collect all words that start with given prefix.
     * 
     * @param node
     * @param prefix
     * @param results
     */
    private void collect(Node node, StringBuilder prefix, Queue<String> results) {
        if (node == null) {
            return;
        }
        if (node.isString) {
            results.enqueue(prefix.toString());
        }
        for (char c = 0; c < R; c++) {
            prefix.append((char) (c + 'A'));
            collect(node.next[c], prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

}
