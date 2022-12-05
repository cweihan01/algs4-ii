import edu.princeton.cs.algs4.Picture;

/******************************************************************************
 * Mutable data type that resizes a W-by-H image using the seam-carving
 * technique.
 * <hr>
 * 3 steps in removing a seam:
 * <ol>
 * <li>Energy calculation - the higher the energy of a pixel, the more important
 * it is (less likely to be included in the seam)
 * <li>Seam identification - a horizontal/vertical path of minimum total energy
 * (shortest path in an edge-weighted digraph)
 * <li>Seam removal - remove all pixels along the identified seam
 * </ol>
 * <p>
 * Note: Pictures are accessed via notation (x, y), where x is the column and y
 * is the row (both zero-indexed).
 * 
 ******************************************************************************/
public class SeamCarver {
    private Picture picture;
    private int width;
    private int height;

    /**
     * Creates a seam carver object based on the given picture.
     * 
     * @param picture
     */
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("picture cannot be null");
        }
        this.picture = new Picture(picture); // deep-copy of picture object
        this.width = picture.width();
        this.height = picture.height();
    }

    /**
     * @return current Picture
     */
    public Picture picture() {
        return new Picture(this.picture);
    }

    /**
     * @return width of current Picture
     */
    public int width() {
        return this.width;
    }

    /**
     * @return height of current Picture
     */
    public int height() {
        return this.height;
    }

    /**
     * Computes the energy of a pixel (x, y) using the dual-energy gradient
     * function. Energy of a pixel at the border of the image is defined to be 1000.
     * 
     * @param x - column
     * @param y - row
     * @return energy of given pixel
     */
    public double energy(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            throw new IllegalArgumentException("invalid pixel provided for current image");
        }

        // pixel is at border of image, energy is 1000
        if (x == 0 || x == this.width - 1 || y == 0 || y == this.height - 1) {
            return 1000.0;
        }

        // calculate energy of pixel using dual-gradient energy
        double deltaXSquared = this.computeSingleGradient(
                this.picture.getRGB(x - 1, y), this.picture.getRGB(x + 1, y));
        double deltaYSquared = this.computeSingleGradient(
                this.picture.getRGB(x, y - 1), this.picture.getRGB(x, y + 1));

        return Math.sqrt(deltaXSquared + deltaYSquared);
    }

    /**
     * Helper function for {@link #energy(int, int)}.
     * Computes the square of the x/y-gradient using the differences in
     * red/blue/green components.
     * 
     * @param rgb1 - 32-bit ARGB int for pixel before current pixel
     * @param rgb2 - 32-bit ARGB int for pixel after current pixel
     * @return delta x/y squared
     */
    private double computeSingleGradient(int rgb1, int rgb2) {
        double redDiff = (double) Math.abs(this.getRedFromRGB(rgb1) - this.getRedFromRGB(rgb2));
        double greenDiff = (double) Math.abs(this.getGreenFromRGB(rgb1) - this.getGreenFromRGB(rgb2));
        double blueDiff = (double) Math.abs(this.getBlueFromRGB(rgb1) - this.getBlueFromRGB(rgb2));
        return Math.pow(redDiff, 2) + Math.pow(greenDiff, 2) + Math.pow(blueDiff, 2);
    }

    /* Helper functions to extract R/G/B components from 32-bit ARGB int. */
    private int getRedFromRGB(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    private int getBlueFromRGB(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private int getGreenFromRGB(int rgb) {
        return (rgb >> 0) & 0xFF;
    }

    /**
     * Finds a horizontal seam across the picture.
     * Uses the same method for {@link #findVerticalSeam()}, but transposes the
     * horizontal energies 2D array to that of a vertical one.
     * 
     * @return sequence of (row) indices for horizontal seam
     *         (in increasing column index)
     */
    public int[] findHorizontalSeam() {
        // calculate energies for each pixel, transpose to an equivalent vertical 2D
        // array where the new array is a reflection along the leading diagonal
        double[][] energies = new double[this.width][this.height];
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                energies[x][y] = this.energy(x, y);
            }
        }

        return new VerticalEnergySP(this.height, this.width, energies).shortestPath();
    }

    /**
     * Finds a vertical seam across the picture.
     * 
     * @return sequence of (column) indices for vertical seam
     *         (in increasing row index)
     */
    public int[] findVerticalSeam() {
        // calculate energies for each pixel
        double[][] energies = new double[this.height][this.width];
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                energies[y][x] = this.energy(x, y);
            }
        }
        return new VerticalEnergySP(this.width, this.height, energies).shortestPath();
    }

    /**
     * Removes the horizontal seam from the current picture.
     * Updates the instance picture, width and height variables.
     * 
     * @param seam - sequence of indices of horizontal seam
     */
    public void removeHorizontalSeam(int[] seam) {
        this.validateSeam(seam, true);
        if (this.height <= 1) {
            throw new IllegalArgumentException("cannot remove horizontal seam - height is smaller than or equal to 1");
        }

        Picture newPic = new Picture(this.width, this.height - 1);
        for (int col = 0; col < this.width; col++) {
            int oldRow = 0;
            int skipRow = seam[col];
            for (int row = 0; row < this.height - 1; row++) {
                // check if we have advanced past the seam to be removed
                if (oldRow == skipRow) {
                    oldRow++;
                }
                newPic.set(col, row, this.picture.get(col, oldRow++));
            }
        }
        this.picture = newPic;
        this.height--;
    }

    /**
     * Removes the vertical seam from the current picture.
     * Updates the instance picture, width and height variables.
     * 
     * @param seam - sequence of indices of vertical seam
     */
    public void removeVerticalSeam(int[] seam) {
        this.validateSeam(seam, false);
        if (this.width <= 1) {
            throw new IllegalArgumentException("cannot remove vertical seam - width is smaller than or equal to 1");
        }

        Picture newPic = new Picture(this.width - 1, this.height);
        for (int row = 0; row < this.height; row++) {
            int oldCol = 0;
            int skipCol = seam[row];
            for (int col = 0; col < this.width - 1; col++) {
                // check if we have advanced past the seam to be removed
                if (oldCol == skipCol) {
                    oldCol++;
                }
                newPic.set(col, row, this.picture.get(oldCol++, row));
            }
        }
        this.picture = newPic;
        this.width--;
    }

    /**
     * Helper method to validate the seam array passed as argument to
     * removeHorizontal/VerticalSeam().
     */
    private void validateSeam(int[] seam, boolean horizontalSeam) {
        if (seam == null) {
            throw new IllegalArgumentException("seam array is null");
        }

        // ensure that array is of correct length
        if ((horizontalSeam && seam.length != this.width) ||
                (!horizontalSeam && seam.length != this.height)) {
            throw new IllegalArgumentException("seam array is of incorrect length");
        }

        for (int i = 0; i < seam.length; i++) {
            int entry = seam[i];
            if (entry < 0) {
                throw new IllegalArgumentException("seam entry is out of prescribed range");
            }

            // entry is beyond picture range
            if (horizontalSeam && entry >= this.height || !horizontalSeam && entry >= this.width) {
                throw new IllegalArgumentException("seam entry is out of prescribed range");
            }

            // two adjacent entries differ by more than 1
            if (i > 0 && Math.abs(entry - seam[i - 1]) > 1) {
                throw new IllegalArgumentException("two adjacent seam entries differ by more than 1");
            }
        }
    }

    /**
     * Unit testing (optional)
     * 
     * @param args
     */
    public static void main(String[] args) {
        // unit testing

    }
}
