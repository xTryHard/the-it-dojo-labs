import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns raw matches into FeatureRows by replaying history in date order.
 *
 * The one rule that matters: for each match we READ the current Elo/form state
 * to build its feature row, and only THEN update the state with the result.
 * Reading before updating is what keeps a match's features strictly in its past
 * — flip the order and you leak the outcome into its own features.
 *
 * Played matches build features AND advance the ratings. Upcoming fixtures
 * (NA scores) build features from whatever history exists at that point but
 * never update anything — there is no result to learn from.
 */
public class FeatureEngineering {

    public static final int FORM_WINDOW = 5;
    public static final double ELO_K = 32.0;

    public record Result(List<FeatureRow> played, List<FeatureRow> upcoming) {}

    public static Result build(List<Match> all, TeamNames names) {
        // Chronological replay is only correct in date order.
        List<Match> sorted = new ArrayList<>(all);
        sorted.sort(Comparator.comparing(Match::date));

        EloRating elo = new EloRating(ELO_K);
        RecentForm form = new RecentForm(FORM_WINDOW);

        List<FeatureRow> played = new ArrayList<>();
        List<FeatureRow> upcoming = new ArrayList<>();

        for (Match m : sorted) {
            LocalDate date = LocalDate.parse(m.date());
            String home = names.canonical(m.homeTeam(), date);
            String away = names.canonical(m.awayTeam(), date);

            // READ state first — these are the pre-match features.
            FeatureRow row = new FeatureRow(
                    m.date(), home, away,
                    elo.rating(home) - elo.rating(away),
                    form.winRate(home), form.winRate(away),
                    form.avgGoalDiff(home), form.avgGoalDiff(away),
                    m.neutral() ? 1.0 : 0.0,
                    m.isPlayed() ? m.homeWin() : null);

            if (m.isPlayed()) {
                played.add(row);
                // THEN update — this result is now part of the past for later matches.
                elo.update(home, away, m.homeScore(), m.awayScore());
                form.update(home, away, m.homeScore(), m.awayScore());
            } else {
                upcoming.add(row);
            }
        }

        return new Result(played, upcoming);
    }
}
