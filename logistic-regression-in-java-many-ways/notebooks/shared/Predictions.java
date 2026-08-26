import java.util.List;

/**
 * Pretty-prints the model's calls on the upcoming fixtures — the payoff of the
 * whole post. Each library produces a home-win probability per fixture; this
 * formats them identically so the four notebooks end the same way.
 *
 * These 44 matches have no result in the data (NA scores): they're the 2026 FIFA
 * World Cup group stage (June 19-27). That's the point of the exercise — a model
 * is for matches you haven't seen the result of. This shipped while the group
 * stage was still being played, so the outcomes were genuinely unknown; whenever
 * you run it, you can check the calls against how those matches actually went.
 */
public class Predictions {

    /**
     * @param fixtures the upcoming FeatureRows (label is null)
     * @param probs    predicted P(home win), aligned with fixtures
     */
    public static void print(List<FeatureRow> fixtures, double[] probs) {
        if (fixtures.size() != probs.length) {
            throw new IllegalArgumentException("fixtures and probs differ in length");
        }
        System.out.printf("%-12s %-22s %-22s %8s   %s%n",
                "date", "home", "away", "P(home)", "call");
        for (int i = 0; i < fixtures.size(); i++) {
            FeatureRow f = fixtures.get(i);
            double p = probs[i];
            // Target is strictly "home win", so p < 0.5 just means the home win
            // is not favored — it covers a draw or an away win, not an away win
            // specifically. Keep the call honest about that.
            String call = p >= 0.5
                    ? f.homeTeam() + " win"
                    : "no home win";
            System.out.printf("%-12s %-22s %-22s %7.1f%%   %s%n",
                    f.date(), f.homeTeam(), f.awayTeam(), p * 100, call);
        }
    }
}
