import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BoggleSolver {
    // use TrieSet over TST for faster indexing (26-way) at cost of space
    // use TrieSet over TrieST since we just need to know if word is inside
    private final TrieSet26 dictionary;

    // each cell has up to 8 neighbors, traverse from top left clockwise
    private final int[] rowD = { -1, -1, -1, 0, 1, 1, 1, 0 };
    private final int[] colD = { -1, 0, 1, 1, 1, 0, -1, -1 };

    /**
     * Initializes the data structure using the given array of strings in the
     * dictionary.
     * 
     * @param dictionary - array of Strings for each word in dictionary
     */
    public BoggleSolver(String[] dictionary) {
        this.dictionary = new TrieSet26();
        for (String word : dictionary) {
            this.dictionary.add(word);
        }
    }

    /**
     * Returns the set of all valid words in the given Boggle board as an Iterable.
     * A valid word must contain at least 3 letters.
     * 
     * @param board - a BoggleBoard
     * @return Iterable containing all valid words in the board
     */
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        final int m = board.rows();
        final int n = board.cols();

        TrieSet26 validWords = new TrieSet26();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                this.dfs(board, i, j, new boolean[m][n], validWords, "");
            }
        }
        return validWords;
    }

    /**
     * Depth-first-search method.
     * Given a starting character (at coordinates {@code row}, {@code col}),
     * recursively searches all possible words from that character.
     * Searched characters are not reused.
     * If the prefix formed is not a prefix in the dictionary, dfs skips to the next
     * prefix.
     * 
     * @param board
     * @param row
     * @param col
     * @param visited
     * @param validWords
     * @param wordSoFar
     */
    private void dfs(BoggleBoard board, int row, int col, boolean[][] visited,
            TrieSet26 validWords, String wordSoFar) {

        visited[row][col] = true;
        // get current wordSoFar, accounting for special Qu case
        char c = board.getLetter(row, col);
        if (c == 'Q') {
            wordSoFar += "QU";
        } else {
            wordSoFar += c;
        }

        // skip search down this path if prefix not in dictionary
        if (!this.dictionary.containsPrefix(wordSoFar)) {
            visited[row][col] = false;
            return;
        }

        // check if current word in dictionary
        if (wordSoFar.length() >= 3 && this.dictionary.contains(wordSoFar)) {
            validWords.add(wordSoFar);
        }

        // 8 possible neighbors
        for (int i = 0; i < 8; i++) {
            int adjRow = row + this.rowD[i];
            int adjCol = col + this.colD[i];

            if (this.isValidCell(adjRow, adjCol, board.rows(), board.cols()) &&
                    !visited[adjRow][adjCol]) {
                this.dfs(board, adjRow, adjCol, visited, validWords, wordSoFar);
                visited[adjRow][adjCol] = false;
            }
        }
    }

    /**
     * Helper method that checks validity of cell, i.e. if row/col falls within
     * board's range.
     */
    private boolean isValidCell(int row, int col, int m, int n) {
        if (row < 0 || row >= m || col < 0 || col >= n) {
            return false;
        }
        return true;
    }

    /**
     * Scores are as such:
     * <ul>
     * <li>length <= 2: 0
     * <li>length = 3/4: 1
     * <li>length = 5: 2
     * <li>length = 6: 3
     * <li>length = 7: 5
     * <li>length >= 8: 11
     * </ul>
     * <hr>
     * 
     * @param word - a String containing letters A-Z
     * @return score of given word if it is in the dictionary, zero otherwise
     */
    public int scoreOf(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("word is null");
        }

        if (!this.dictionary.contains(word)) {
            return 0;
        }

        // obtain word length, no need to account for special Qu case again as the word
        // should already contain Qu as 2 chars when coming from dfs
        int length = word.length();

        switch ((length <= 2) ? "ZERO"
                : (length == 3 || length == 4) ? "ONE"
                        : (length == 5) ? "TWO"
                                : (length == 6) ? "THREE"
                                        : (length == 7) ? "FIVE" : "ELEVEN") {
            case "ZERO":
                return 0;
            case "ONE":
                return 1;
            case "TWO":
                return 2;
            case "THREE":
                return 3;
            case "FIVE":
                return 5;
            case "ELEVEN":
                return 11;
        }

        return 0;
    }

    /**
     * Test client.
     * 
     * @param args
     */
    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        solver.getAllValidWords(board);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
}
