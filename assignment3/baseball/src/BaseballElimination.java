import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {

    // INSTANCE VARIABLES //
    /**
     * Each team is indexed in this array by their relative position given in the
     * datafile.
     */
    private final String[] teams;

    /**
     * Maps each team name to its corresponding index. Used to retrieve data from
     * this data structure when passed in an input team name.
     */
    private final Map<String, Integer> teamToIndex;

    /**
     * Number of teams in division.
     */
    private final int N;

    /**
     * 2D array with win-loss-remaining games for each team.
     */
    private final int[][] wlr;

    /**
     * 2D array containing the number of games for each team against other teams in
     * the division.
     */
    private final int[][] games;

    /**
     * Ford Fulkerson structure to solve maxflow/mincut problem.
     */
    private FordFulkerson fulkerson;

    /**
     * Certificate of elimination to determine subset R of teams that eliminates a
     * given team.
     */
    private Set<String> certOfElim;

    // CLASS METHODS //
    /**
     * Creates a baseball division from given filename in specified format.
     * 
     * @param filename - name of input file
     */
    public BaseballElimination(String filename) {
        In reader = new In(filename);
        this.N = reader.readInt();
        this.teams = new String[N];
        this.teamToIndex = new HashMap<>(N);
        this.wlr = new int[N][3];
        this.games = new int[N][N];

        for (int i = 0; i < N; i++) {
            this.teams[i] = reader.readString();
            this.teamToIndex.put(this.teams[i], i);

            // populate wlr
            for (int j = 0; j < 3; j++) {
                this.wlr[i][j] = reader.readInt();
            }

            // populate games
            // this.gameVertices = 0;
            for (int j = 0; j < N; j++) {
                int g = reader.readInt();
                this.games[i][j] = g;
                // if (g > 0) {
                // gameVertices++;
                // }
            }
            // this.gameVertices /= 2;
        }
    }

    /**
     * @return number of teams in division
     */
    public int numberOfTeams() {
        return this.N;
    }

    /**
     * 
     * @return all teams
     */
    public Iterable<String> teams() {
        return Arrays.asList(this.teams);
    }

    /**
     * @param team
     * @return number of wins for given team
     */
    public int wins(String team) {
        this.validate(team);
        return this.wlr[this.teamToIndex.get(team)][0];
    }

    /**
     * @param team
     * @return number of losses for given team
     */
    public int losses(String team) {
        this.validate(team);
        return this.wlr[this.teamToIndex.get(team)][1];
    }

    /**
     * @param team
     * @return number of remaining games for given team
     */
    public int remaining(String team) {
        this.validate(team);
        return this.wlr[this.teamToIndex.get(team)][2];
    }

    /**
     * 
     * @param team1
     * @param team2
     * @return number of remaining games between team1 & team2
     */
    public int against(String team1, String team2) {
        this.validate(team1);
        this.validate(team2);
        return this.games[this.teamToIndex.get(team1)][this.teamToIndex.get(team2)];
    }

    /**
     * 
     * @param team
     * @return true if given team is eliminated
     */
    public boolean isEliminated(String team) {
        this.validate(team);
        final int x = this.teamToIndex.get(team);
        // create new certOfElim for each team x that we are querying
        this.certOfElim = new LinkedHashSet<>();

        // trivial elimination - max number of games team can win is less than the
        // number of wins of another team
        for (String other : this.teams) {
            if (this.wins(team) + this.remaining(team) < this.wins(other)) {
                this.certOfElim.add(other);
                return true;
            }
        }

        // non-trivial elimination - create flow network
        // create GameVertex for each game that does not include team x
        List<GameVertex> gameVertices = new ArrayList<>();
        int totalGamesRemaining = 0;
        for (int i = 0; i < this.N; i++) {
            for (int j = 0; j < this.N; j++) {
                // skip team x and avoid double-counting the same game
                if (x == i || x == j || i >= j) {
                    continue;
                }

                // only add game vertex if there is at least 1 game to be played
                int against = this.games[i][j];
                if (against > 0) {
                    gameVertices.add(new GameVertex(i, j, against));
                    totalGamesRemaining += against;
                }
            }
        }

        // initialize some indices used for flow network
        final int verticesCount = gameVertices.size() + this.N + 2;
        final int s = verticesCount - 2, t = verticesCount - 1;
        // team vertices are indexed by (their division index + offset)
        final int offset = gameVertices.size();

        // DEBUG
        // System.out.println("Game Vertices: ");
        // int idx = 0;
        // for (GameVertex g : gameVertices) {
        // System.out.print("Index " + idx++ + ": ");
        // System.out.println(g.toString());
        // }

        /*
         * Flow network indices:
         * - [0, offset): game vertices (order doesn't matter)
         * - [offset, (offset + N)): team vertices (ordered by division index)
         * - (offset + x): team vertex for team x, not included in network
         * - (offset + N): source vertex s
         * - (offset + N + 1): sink vertex t
         */
        final FlowNetwork network = new FlowNetwork(verticesCount);

        // add edges from s to game vertices, and game vertices to team vertices
        for (int i = 0; i < offset; i++) {
            GameVertex gameVertex = gameVertices.get(i);

            // edge from s to game vertex has capacity equal to games left
            network.addEdge(new FlowEdge(s, i, gameVertex.games));

            // edge from game vertex to each of the 2 team vertices has infinite capacity
            network.addEdge(
                    new FlowEdge(i, gameVertex.team1 + offset, Double.POSITIVE_INFINITY));
            network.addEdge(
                    new FlowEdge(i, gameVertex.team2 + offset, Double.POSITIVE_INFINITY));
        }

        // add edges from team vertices to t, with capacity equal to the minimum wins
        // needed for team x to catch up with team i
        int maxWinsByX = this.wins(team) + this.remaining(team);
        for (int i = 0; i < this.N; i++) {
            if (i == x) {
                continue;
            }
            network.addEdge(new FlowEdge(i + offset, t, maxWinsByX - this.wins(this.teams[i])));
        }

        // create ff structure to solve maxflow problem
        this.fulkerson = new FordFulkerson(network, s, t);

        // find certificate of elimination - what subset R of teams eliminate team x?
        for (int i = 0; i < this.N; i++) {
            if (i == x) {
                continue;
            }
            if (this.fulkerson.inCut(i + offset)) {
                this.certOfElim.add(this.teams[i]);
            }
        }

        // team x is eliminated if maxflow is smaller than total games left between
        // every other team in its division
        return (int) fulkerson.value() != totalGamesRemaining;
    }

    /**
     * Subset R of teams that eliminates given team, null if not eliminated.
     * 
     * @param team
     * @return subset R that eliminates given team
     */
    public Iterable<String> certificateOfElimination(String team) {
        this.validate(team);
        if (!this.isEliminated(team)) {
            return null;
        }
        return this.certOfElim;
    }

    /**
     * Helper method that ensures input String is not null, and is a valid team in
     * division.
     * 
     * @param team
     * @throws IllegalArgumentException if team is null, empty or not in division
     */
    private void validate(String team) {
        if (team == null || team.isEmpty()) {
            throw new IllegalArgumentException("team cannot be null or empty!");
        }
        if (!Arrays.asList(this.teams).contains(team)) {
            throw new IllegalArgumentException("team does not exist in division!");
        }
    }

    /**
     * Game Vertex used in flow network.
     * Represents a game between two teams, and contains the number of these games.
     */
    private class GameVertex {
        int team1;
        int team2;
        int games;

        public GameVertex(int team1, int team2, int games) {
            this.team1 = team1;
            this.team2 = team2;
            this.games = games;
        }

        public String toString() {
            return String.format("Team %d vs Team %d: %d games", team1, team2, games);
        }
    }

    /**
     * Visualize processed data.
     */
    private void printData() {
        System.out.println("Printing teams...");
        System.out.println(this.teams());

        System.out.println("Printing WLR and games...");
        for (int i = 0; i < N; i++) {
            System.out.print(this.teams[i] + "\t");
            for (int j = 0; j < 3; j++) {
                System.out.print(this.wlr[i][j] + " ");
            }
            for (int j = 0; j < N; j++) {
                System.out.print(this.games[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        division.printData();

        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
