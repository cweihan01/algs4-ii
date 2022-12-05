
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Stack;

/**
 * VerticalEnergySP class.
 * <p>
 * Given a 2D array of energy values, finds the shortest vertical path from top
 * to bottom. Traverses the 2D array in topological order, where each energy
 * element only has a directed edge to its 3 immediate neighbors below.
 * <p>
 * To find the horizontal energy SP, use the same class but swap the
 * width/height arguments, and flip the energies 2D array along the leading
 * diagonal.
 */
public class VerticalEnergySP {
    private final int width;
    private final int height;
    private final int size;
    private final double[] energies;

    // use a 1D array for easier manipulation
    private int[] edgeTo;
    private double[] distTo;

    public VerticalEnergySP(int width, int height, double[][] energies) {
        this.width = width;
        this.height = height;
        this.size = width * height;

        // flatten 2D energies array to 1D
        this.energies = new double[this.size];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                this.energies[this.flattenedIndex(col, row)] = energies[row][col];
            }
        }
        this.edgeTo = new int[this.size];
        this.distTo = new double[this.size];

        // initialize all energy distances to infinity except top row which is 1000.0
        for (int i = 0; i < this.width; i++) {
            distTo[i] = 1000.0;
        }
        for (int i = this.width; i < this.size; i++) {
            distTo[i] = Double.POSITIVE_INFINITY;
        }

        // iterate through each energy value in topological order, skipping bottom row
        for (int i = 0; i < this.size - this.width; i++) {
            for (int adj : this.adj(i)) {
                this.relax(i, adj);
            }
        }
    }

    /**
     * Converts a 2D pixel coordinate to a 1D index, where the 2D array is
     * traversed from top to bottom, left to right.
     * 
     * @param col
     * @param row
     * @return a 1D index corresponding to the given col and row
     */
    private int flattenedIndex(int col, int row) {
        assert col >= 0 && col < this.width;
        assert row >= 0 && row < this.height;
        return this.width * row + col;
    }

    /**
     * Helper function to obtain an array containing the indexes that are adjacent
     * to the given index. An element is only adjacent to the 3 elements directly
     * below it (2 neighbors if element is in first or last column, 1 neighbor if
     * only one column).
     * 
     * @param i - flattened index of an energy element, whose adjacent neighbors we
     *          want to find
     * @return an array of flattened indices of the (2 or 3) adjacent neighbors
     */
    private int[] adj(int i) {
        // we should never be asking for neighbors of bottom row elements
        assert i >= 0 && i < this.width * (this.height - 1);

        // if width is 1, return only 1 neighbor below
        if (this.width == 1) {
            return new int[] { i + this.width };
        }

        if (i % this.width == 0) {
            // first column only has 2 neighbors
            return new int[] { i + this.width, i + this.width + 1 };
        } else if (i % this.width == this.width - 1) {
            // last column also has 2 neighbors
            return new int[] { i + this.width - 1, i + this.width };
        } else {
            // every other column has 3 neighbors
            return new int[] { i + this.width - 1, i + this.width, i + this.width + 1 };
        }
    }

    /**
     * Relaxes the edge from element i to its neighbor adj.
     * Updates the distTo and edgeTo instance variables if a shorter energy path is
     * found from i to adj, using the energy value for adj.
     * 
     * @param i   - flattened index of any element except bottom row
     * @param adj - flattened index of any element except top row, which is adjacent
     *            to i
     */
    private void relax(int i, int adj) {
        // i should not include the last row, and adj should not include the first row
        assert i >= 0 && i < this.size - this.width;
        assert adj >= this.width && adj < this.size;

        if (this.distTo[adj] > this.distTo[i] + this.energies[adj]) {
            distTo[adj] = this.distTo[i] + this.energies[adj];
            edgeTo[adj] = i;
        }
    }

    /**
     * Finds the minimium distTo among all the total energy paths in the bottom row.
     * Then returns the shortest path to that energy element using the 2D coordinate
     * system, but only including the 2D column index (row index is trivial for
     * vertical paths).
     * 
     * @return an array of the column number where the shortest path is found,
     *         arranged in increasing row numbers
     */
    public int[] shortestPath() {
        // get element index in last row which has smallest total energy
        int minIndex = this.size - this.width;
        double minDist = this.distTo[minIndex];
        for (int i = this.size - this.width + 1; i < this.size; i++) {
            if (this.distTo[i] < minDist) {
                minDist = this.distTo[i];
                minIndex = i;
            }
        }

        // get a stack of the shortest path 1D indices
        Stack<Integer> pathStack = new Stack<>();
        for (int i = minIndex; pathStack.size() != this.height; i = this.edgeTo[i]) {
            pathStack.push(i);
        }
        assert pathStack.size() == this.height;

        // convert the 1D flattened index to the 2D column index
        // (row index is not needed)
        int[] pathArray = new int[this.height];
        for (int i = 0; i < this.height; i++) {
            pathArray[i] = pathStack.pop() % this.width;
        }
        return pathArray;
    }

    /**
     * Unit testing for VerticalEnergySP class.
     */
    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);
        SeamCarver sc = new SeamCarver(picture);
        double[][] energies = new double[picture.height()][picture.width()];
        for (int y = 0; y < picture.height(); y++) {
            for (int x = 0; x < picture.width(); x++) {
                energies[y][x] = sc.energy(x, y);
            }
        }

        System.out.println("PRINTING ENERGY 2D ARRAY...");
        for (int i = 0; i < picture.height(); i++) {
            for (int j = 0; j < picture.width(); j++) {
                System.out.print(energies[i][j]);
                System.out.print("\t");
            }
            System.out.println();
        }

        int width = picture.width();
        int height = picture.height();
        int size = width * height;
        VerticalEnergySP energyPath = new VerticalEnergySP(width, height, energies);
        double[] energiesFlattened = energyPath.energies;

        System.out.println("PRINTING ENERGY 1D FLATTENED ARRAY...");
        for (int i = 0; i < size; i++) {
            if (i % width == 0) {
                System.out.println();
            }
            System.out.print(energiesFlattened[i]);
            System.out.print("\t");
        }

        System.out.println("PRINTING SHORTEST PATH...");
        int[] shortestPath = energyPath.shortestPath();
        for (int i = 0; i < height; i++) {
            System.out.println(shortestPath[i]);
        }
    }
}
