import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

/**
 * The WordNet digraph. Your first task is to build the WordNet digraph: each
 * vertex v is an integer that represents a synset, and each directed edge v→w
 * represents that w is a hypernym of v.
 * 
 * <p>
 * The WordNet digraph is a rooted DAG: it is acyclic and has one vertex — the
 * root — that is an ancestor of every other vertex. However, it is not
 * necessarily a tree because a synset can have more than one hypernym.
 * 
 * <hr>
 * <i>Corner cases.</i> Throw an IllegalArgumentException in the following
 * situations:
 * <ul>
 * <li>Any argument to the constructor or an instance method is null
 * <li>The input to the constructor does not correspond to a rooted DAG.
 * <Li>Any of the noun arguments in distance() or sap() is not a WordNet noun.
 * </ul>
 * You may assume that the input files are in the specified format.
 * 
 * <hr>
 * <i>Performance requirements:</i>
 * <ul>
 * <li>Your data type should use space linear in the input size (size of synsets
 * and hypernyms files).
 * <li>The constructor should take time linearithmic (or better) in the input
 * size.
 * <li>The method isNoun() should run in time logarithmic (or better) in the
 * number of nouns.
 * <li>The methods distance() and sap() should run in time linear in the size of
 * the WordNet digraph.
 * </ul>
 * For the analysis, assume that the number of nouns per synset is bounded by a
 * constant.
 */
public class WordNet {
    /**
     * Map each synset id to a list of all its synset nouns.
     * Use TreeMap since integer ids are naturally ordered.
     */
    private final Map<Integer, String> idToNounsMap = new TreeMap<>();

    /**
     * Map each noun to a list of its synset id.
     * We use a list of ids in case some nouns appear more than once with different
     * meanings (eg. worm).
     * Use HashMap for constant lookup time of strings.
     */
    private final Map<String, List<Integer>> nounToIdMap = new HashMap<>();

    /**
     * SAP object to calculate distance/ancestor between two nouns.
     */
    private final SAP sap;

    /**
     * WordNet constructor takes the name of the two input files.
     * Stores synsets data in instance variables {@code idToNounsMap} and
     * {@code nounToIdMap}.
     * Creates a digraph from the hypernyms file, and a SAP object from the digraph.
     * 
     * @param synsets   - relative path to a synsets file;
     *                  format of file is: synset id, synset (synset) ..., gloss
     * @param hypernyms - relative path to a hypernyms file;
     *                  format of file is: synset id, hypernym id, (hypernym id) ...
     */
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null || synsets.isEmpty() || hypernyms.isEmpty()) {
            throw new IllegalArgumentException("input files cannot be null!");
        }

        /*
         * map each synset id to its synonym set
         * read from `synsets.txt`, where the format is: synset id, synset(s), gloss
         */
        In synsetsIn = new In(synsets);
        while (synsetsIn.hasNextLine()) {
            String[] tokens = synsetsIn.readLine().split(",");
            final int id = Integer.parseInt(tokens[0]);
            final String nouns = tokens[1];
            this.idToNounsMap.put(id, nouns);

            for (String s : nouns.split(" ")) {
                this.nounToIdMap.putIfAbsent(s, new ArrayList<>());
                this.nounToIdMap.get(s).add(id);
            }
        }
        synsetsIn.close();

        /*
         * create a digraph where each directed edge v→w represents that w is a hypernym
         * of v
         * read from hypernyms.txt, where the format is: synset id, hypernym id(s)
         */
        Digraph digraph = new Digraph(this.idToNounsMap.size());
        In hypernymsIn = new In(hypernyms);
        while (hypernymsIn.hasNextLine()) {
            String[] tokens = hypernymsIn.readLine().split(",");
            final int id = Integer.parseInt(tokens[0]);

            for (int i = 1; i < tokens.length; i++) {
                digraph.addEdge(id, Integer.parseInt(tokens[i]));
            }
        }
        hypernymsIn.close();

        // construct a sap for this digraph
        this.sap = new SAP(digraph);
    }

    /**
     * Returns all WordNet nouns.
     * 
     * @return an Iterable containing all nouns
     */
    public Iterable<String> nouns() {
        return this.nounToIdMap.keySet();
    }

    /**
     * Returns true if noun in WordNet.
     * 
     * @param word
     * @return true if noun in WordNet
     */
    public boolean isNoun(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("word is null or empty!");
        }
        return this.nounToIdMap.containsKey(word);
    }

    /**
     * Distance of shortest ancestral path between nounA and nounB.
     * Checks for all paths between both given nouns (including repeated
     * occurences).
     * 
     * @param nounA
     * @param nounB
     * @return shortest distance between nounA and nounB; -1 if no path exists
     */
    public int distance(String nounA, String nounB) {
        this.validateNoun(nounA);
        this.validateNoun(nounB);
        assert sap != null;

        List<Integer> idA = this.nounToIdMap.get(nounA);
        List<Integer> idB = this.nounToIdMap.get(nounB);

        return sap.length(idA, idB);
    }

    /**
     * A synset that is the common ancestor of nounA and nounB in a shortest
     * ancestral path.
     * 
     * @param nounA
     * @param nounB
     * @return a String that includes all synset nouns that are the common ancestor
     *         in the shortest ancestral path
     */
    public String sap(String nounA, String nounB) {
        this.validateNoun(nounA);
        this.validateNoun(nounB);
        assert sap != null;

        List<Integer> idA = this.nounToIdMap.get(nounA);
        List<Integer> idB = this.nounToIdMap.get(nounB);

        final int idAncestor = sap.ancestor(idA, idB);
        if (idAncestor == -1) {
            return null;
        }

        return idToNounsMap.get(idAncestor);
    }

    /**
     * Helper function to ensure noun is valid and inside WordNet.
     * 
     * @param noun
     * @throws IllegalArgumentException null or empty noun
     * @throws IllegalArgumentException noun not in WordNet
     */
    private void validateNoun(String noun) {
        if (noun == null || noun.isEmpty()) {
            throw new IllegalArgumentException("noun cannot be null or empty!");
        }
        if (!isNoun(noun)) {
            throw new IllegalArgumentException(
                    String.format("noun '%s' does not exist in digraph!", noun));
        }
    }

    /**
     * Unit testing for WordNet class.
     * 
     * @param args
     */
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        /*
         * test that we process input files correctly
         */
        // System.out.println(wordnet.digraph);
        // System.out.println(wordnet.idToNounsMap);
        // System.out.println(wordnet.nounToIdMap);

        /*
         * test shortest path between two nouns in wordnet
         */
        StdOut.println("Enter two nouns to get their shortest ancestral path (case-sensitive):");
        while (!StdIn.isEmpty()) {
            String nounA = StdIn.readString();
            String nounB = StdIn.readString();
            int dist = wordnet.distance(nounA, nounB);
            String sap = wordnet.sap(nounA, nounB);
            StdOut.printf("shortest distance = %d, common ancestor = %s\n", dist, sap);
        }
    }
}