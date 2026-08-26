import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks each team's last N results to produce "recent form" features:
 * a rolling win rate and a rolling average goal difference.
 *
 * Like Elo, this is updated as we replay matches in date order. A team with no
 * history yet returns neutral defaults (0.5 win rate, 0.0 goal diff) so early
 * matches don't blow up.
 */
public class RecentForm {

    private final int window;
    // per team: recent win/draw/loss as points (1.0/0.5/0.0)
    private final Map<String, Deque<Double>> results = new HashMap<>();
    // per team: recent goal differences (from that team's perspective)
    private final Map<String, Deque<Integer>> goalDiffs = new HashMap<>();

    public RecentForm(int window) {
        this.window = window;
    }

    public double winRate(String team) {
        Deque<Double> q = results.get(team);
        if (q == null || q.isEmpty()) {
            return 0.5;
        }
        double sum = 0.0;
        for (double v : q) {
            sum += v;
        }
        return sum / q.size();
    }

    public double avgGoalDiff(String team) {
        Deque<Integer> q = goalDiffs.get(team);
        if (q == null || q.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (int v : q) {
            sum += v;
        }
        return sum / q.size();
    }

    public void update(String home, String away, int homeScore, int awayScore) {
        double homePoints = homeScore > awayScore ? 1.0
                : homeScore < awayScore ? 0.0
                : 0.5;
        record(home, homePoints, homeScore - awayScore);
        record(away, 1.0 - homePoints, awayScore - homeScore);
    }

    private void record(String team, double points, int goalDiff) {
        Deque<Double> r = results.computeIfAbsent(team, t -> new ArrayDeque<>());
        Deque<Integer> g = goalDiffs.computeIfAbsent(team, t -> new ArrayDeque<>());
        r.addLast(points);
        g.addLast(goalDiff);
        if (r.size() > window) {
            r.removeFirst();
        }
        if (g.size() > window) {
            g.removeFirst();
        }
    }
}
