import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Splits played FeatureRows into train and test sets — chronologically.
 *
 * Why chronological and not a random shuffle? Our features describe a team's
 * state up to a point in time, and the real job is to predict matches that
 * haven't happened yet. So we train on the past and test on the most recent
 * slice. That mirrors deployment: you never get to peek at the future. A random
 * split would scatter recent matches into training and quietly flatter the
 * scores.
 *
 * We keep the split in domain terms — List<FeatureRow> in, List<FeatureRow> out
 * — and let each library's notebook convert to its own structure. That
 * conversion is the per-library adapter, and showing it is half the point of
 * this post.
 */
public class TrainTestSplit {

    public record Split(List<FeatureRow> train, List<FeatureRow> test) {}

    /** trainFraction of the earliest matches go to train, the rest to test. */
    public static Split chronological(List<FeatureRow> played, double trainFraction) {
        List<FeatureRow> sorted = new ArrayList<>(played);
        sorted.sort(Comparator.comparing(FeatureRow::date));

        int cutoff = (int) Math.round(sorted.size() * trainFraction);
        List<FeatureRow> train = new ArrayList<>(sorted.subList(0, cutoff));
        List<FeatureRow> test = new ArrayList<>(sorted.subList(cutoff, sorted.size()));
        return new Split(train, test);
    }

    // --- helpers most libraries want: feature matrix + label vector ---

    public static double[][] toX(List<FeatureRow> rows) {
        double[][] x = new double[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            x[i] = rows.get(i).features();
        }
        return x;
    }

    /** Labels as 0/1 ints. Assumes rows have a non-null label (played matches). */
    public static int[] toY(List<FeatureRow> rows) {
        int[] y = new int[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            y[i] = rows.get(i).homeWin() ? 1 : 0;
        }
        return y;
    }
}
