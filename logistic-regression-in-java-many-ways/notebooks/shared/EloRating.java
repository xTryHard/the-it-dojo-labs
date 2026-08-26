import java.util.HashMap;
import java.util.Map;

/**
 * A minimal Elo rating engine. Every team starts at 1500. After each match the
 * winner takes points from the loser; the size of the swing depends on how
 * surprising the result was (beat a much stronger team, gain more).
 *
 * This is the classic chess Elo, applied to football. It is deliberately simple
 * — no margin-of-victory or home-field term in the rating itself, because we
 * feed "neutral" to the model as its own feature and want the Elo gap to mean
 * just "relative strength."
 */
public class EloRating {

    private static final double INITIAL = 1500.0;
    private final double k;
    private final Map<String, Double> ratings = new HashMap<>();

    public EloRating(double k) {
        this.k = k;
    }

    public double rating(String team) {
        return ratings.getOrDefault(team, INITIAL);
    }

    /** Expected score (win probability proxy) for A against B. */
    public static double expected(double ratingA, double ratingB) {
        return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0));
    }

    /**
     * Update both teams after a result. homeScore/awayScore decide the actual
     * score: 1.0 win, 0.5 draw, 0.0 loss.
     */
    public void update(String home, String away, int homeScore, int awayScore) {
        double rHome = rating(home);
        double rAway = rating(away);

        double actualHome = homeScore > awayScore ? 1.0
                : homeScore < awayScore ? 0.0
                : 0.5;
        double expectedHome = expected(rHome, rAway);

        double delta = k * (actualHome - expectedHome);
        ratings.put(home, rHome + delta);
        ratings.put(away, rAway - delta);
    }
}
