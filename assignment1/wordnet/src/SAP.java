import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

/**
 * Shortest Ancestral Path class.
 * <p>
 * An ancestral path between two vertices v and w in a digraph is a directed
 * path from v to a common ancestor x, together with a directed path from w to
 * the same ancestor x. A shortest ancestral path is an ancestral path of
 * minimum total length. We refer to the common ancestor in a shortest ancestral
 * path as a shortest common ancestor. Note also that an ancestral path is a
 * path, but not a directed path.
 */
public class SAP {
    private final Digraph digraph;
    private final int vertices;

    /**
     * SAP constructor takes a digraph (not necessarily a DAG).
     * 
     * @param G - a Digraph
     */
    public SAP(Digraph G) {
        if (G == null) {
            throw new IllegalArgumentException("digraph cannot be null!");
        }
        this.digraph = new Digraph(G); // create a deepcopy of digraph so it is immutable
        this.vertices = G.V();
    }

    /**
     * Computes the length of shortest ancestral path between two vertices v and w
     * in the digraph.
     * 
     * @param v - a vertex in digraph
     * @param w - a vertex in digraph
     * @return length of shortest ancestral path; -1 if no path exists
     */
    public int length(int v, int w) {
        BreadthFirstDirectedPaths vPaths = new BreadthFirstDirectedPaths(this.digraph, v);
        BreadthFirstDirectedPaths wPaths = new BreadthFirstDirectedPaths(this.digraph, w);

        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < this.vertices; i++) {
            // there is a path from both v and w to i -- common ancestor exist
            if (vPaths.hasPathTo(i) && wPaths.hasPathTo(i)) {
                int dist = vPaths.distTo(i) + wPaths.distTo(i);
                if (dist < minDist) {
                    minDist = dist; // only save shortest ancestral path
                }
            }
        }
        return (minDist < Integer.MAX_VALUE) ? minDist : -1;
    }

    /**
     * Finds a common ancestor of v and w that participates in a shortest ancestral
     * path.
     * 
     * @param v - a vertex in digraph
     * @param w - a vertex in digraph
     * @return the common ancestor that is in the shortest ancestral path; -1 if no
     *         path exists
     */
    public int ancestor(int v, int w) {
        BreadthFirstDirectedPaths vPaths = new BreadthFirstDirectedPaths(this.digraph, v);
        BreadthFirstDirectedPaths wPaths = new BreadthFirstDirectedPaths(this.digraph, w);

        final int minDist = this.length(v, w);
        if (minDist == -1) {
            return -1;
        }

        for (int i = 0; i < this.vertices; i++) {
            // there is a path from both v and w to i -- common ancestor exist
            if (vPaths.hasPathTo(i) && wPaths.hasPathTo(i)) {
                if (vPaths.distTo(i) + wPaths.distTo(i) == minDist) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Computes the length of shortest ancestral path any vertex in v and any vertex
     * in w.
     * 
     * @param v - an Iterable containing vertices in digraph
     * @param w - an Iterable containing vertices in digraph
     * @return length of shortest ancestral path; -1 if no path exists
     */
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        this.validateVertices(v, w);
        BreadthFirstDirectedPaths vPaths = new BreadthFirstDirectedPaths(this.digraph, v);
        BreadthFirstDirectedPaths wPaths = new BreadthFirstDirectedPaths(this.digraph, w);

        int minDist = Integer.MAX_VALUE;
        for (int i = 0; i < this.vertices; i++) {
            if (vPaths.hasPathTo(i) && wPaths.hasPathTo(i)) {
                int dist = vPaths.distTo(i) + wPaths.distTo(i);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }
        return (minDist < Integer.MAX_VALUE) ? minDist : -1;
    }

    /**
     * Finds a common ancestor of the shortest ancestral path between any vertex in
     * v and any vertex in w.
     * 
     * @param v - an Iterable containing vertices in digraph
     * @param w - an Iterable containing vertices in digraph
     * @return the common ancestor in the shortest ancestral path between v and w;
     *         -1 if no path exists
     */
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        this.validateVertices(v, w);
        BreadthFirstDirectedPaths vPaths = new BreadthFirstDirectedPaths(this.digraph, v);
        BreadthFirstDirectedPaths wPaths = new BreadthFirstDirectedPaths(this.digraph, w);

        final int minDist = this.length(v, w);
        for (int i = 0; i < this.vertices; i++) {
            if (vPaths.hasPathTo(i) && wPaths.hasPathTo(i)) {
                if (vPaths.distTo(i) + wPaths.distTo(i) == minDist) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void validateVertices(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new IllegalArgumentException("argument is null!");
        }
        for (Integer val : v) {
            if (val == null) {
                throw new IllegalArgumentException("value inside iterable is null!");
            }
        }
        for (Integer val : w) {
            if (val == null) {
                throw new IllegalArgumentException("value inside iterable is null!");
            }
        }
    }

    /**
     * Unit testing of the SAP class.
     * 
     * @param args
     */
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
