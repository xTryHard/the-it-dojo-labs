/**
 * The engineered feature vector for one match — the single table every library
 * in this post consumes. Built once in FeatureEngineering, then handed to
 * DeepNetts, Tribuo, Weka, and Smile unchanged.
 *
 * All features are known *before* kickoff. The label (homeWin) is known only for
 * played matches; for upcoming fixtures it is null and the model fills it in.
 */
public record FeatureRow(
        String date,
        String homeTeam,
        String awayTeam,
        double eloDiff,        // home Elo minus away Elo
        double homeWinRate,    // home team's recent rolling win rate
        double awayWinRate,    // away team's recent rolling win rate
        double homeGoalDiff,   // home team's recent avg goal difference
        double awayGoalDiff,   // away team's recent avg goal difference
        double neutral,        // 1.0 if neutral venue, else 0.0
        Boolean homeWin) {     // label: true/false for played, null for upcoming

    /** Feature values in a fixed order — handy for libraries that want double[]. */
    public double[] features() {
        return new double[] {
            eloDiff, homeWinRate, awayWinRate, homeGoalDiff, awayGoalDiff, neutral
        };
    }

    public static String[] featureNames() {
        return new String[] {
            "eloDiff", "homeWinRate", "awayWinRate", "homeGoalDiff", "awayGoalDiff", "neutral"
        };
    }
}
