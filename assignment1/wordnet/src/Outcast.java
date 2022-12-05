import java.util.HashMap;
import java.util.Map;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

/**
 * Outcast class.
 * <p>
 * <i>Outcase detection.</i> Given a list of WordNet nouns x1, x2, ..., xn,
 * which noun is the least related to the others? To identify an outcast,
 * compute the sum of the distances between each noun and every other one:
 * {@code di = distance(xi, x1) + distance(xi, x2) + ... + distance(xi, xn)} and
 * return a noun {@code xt} for which {@code dt} is maximum. Note that
 * {@code distance(xi, xi) = 0}, so it will not contribute to the sum.
 */
public class Outcast {
    private final WordNet wordnet;

    /**
     * Outcast constructor takes a WordNet object.
     * 
     * @param wordnet
     */
    public Outcast(WordNet wordnet) {
        if (wordnet == null) {
            throw new IllegalArgumentException("wordnet is null!");
        }
        this.wordnet = wordnet;
    }

    /**
     * Given an array of WordNet nouns, return an outcast.
     * Calculates the distance between a noun and every other noun.
     * 
     * @param nouns
     * @return the noun with the highest sum of distances to every other noun
     */
    public String outcast(String[] nouns) {
        assert wordnet != null;
        if (nouns == null || nouns.length == 0) {
            throw new IllegalArgumentException("nouns array cannot be null or empty!");
        }

        // return the only noun if array is of length 1
        if (nouns.length == 1) {
            return nouns[0];
        }

        /*
         * calculate distances between a noun and every other noun
         * store in a 2d array so we do not call distance between two nouns more than
         * once
         */
        final int length = nouns.length;
        int[][] distances = new int[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (j < i) {
                    continue;
                } else if (j == i) {
                    distances[i][j] = 0;
                    distances[j][i] = 0;
                } else {
                    int dist = this.wordnet.distance(nouns[i], nouns[j]);
                    distances[i][j] = dist;
                    distances[j][i] = dist;
                }
            }
        }

        /*
         * compute the sum of distances to other nouns for each noun
         * map each noun to its sum
         */
        Map<String, Integer> sumMap = new HashMap<>();
        for (int i = 0; i < length; i++) {
            int sum = 0;
            for (int j = 0; j < length; j++) {
                sum += distances[i][j];
            }
            sumMap.put(nouns[i], sum);
        }

        // get the maximum sum value and return its key
        return sumMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }

    /**
     * Unit testing of the Outcast class.
     * 
     * @param args
     */
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}