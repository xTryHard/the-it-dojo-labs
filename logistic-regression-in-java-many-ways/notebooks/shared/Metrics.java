import java.util.List;

/**
 * One fair scoreboard, computed the same way for every library.
 *
 * Each library has its own evaluator, but they report differently and round
 * differently. To compare four libraries honestly we score them all through
 * this one class, fed the same thing: the predicted probability of a home win
 * and the actual 0/1 outcome.
 *
 * We report two kinds of number:
 *   - Threshold metrics (accuracy, precision, recall, F1) at p >= 0.5 — "was the
 *     call right?"
 *   - Probability metrics (log-loss, Brier) — "how good were the probabilities?"
 *     Logistic regression's whole selling point is calibrated probabilities, so
 *     a model can have the same accuracy as another but better (lower) log-loss
 *     because it's more honest about its confidence.
 */
public class Metrics {

    public final double accuracy;
    public final double precision;
    public final double recall;
    public final double f1;
    public final double logLoss;
    public final double brier;
    public final int n;

    private Metrics(double accuracy, double precision, double recall,
                    double f1, double logLoss, double brier, int n) {
        this.accuracy = accuracy;
        this.precision = precision;
        this.recall = recall;
        this.f1 = f1;
        this.logLoss = logLoss;
        this.brier = brier;
        this.n = n;
    }

    /**
     * @param probs predicted P(home win) per match
     * @param actual actual outcome per match (1 = home win, 0 = not)
     */
    public static Metrics from(double[] probs, int[] actual) {
        if (probs.length != actual.length) {
            throw new IllegalArgumentException("probs and actual differ in length");
        }
        int n = probs.length;
        int tp = 0, fp = 0, tn = 0, fn = 0;
        double logLossSum = 0.0, brierSum = 0.0;
        double eps = 1e-15; // clamp so log(0) doesn't blow up

        for (int i = 0; i < n; i++) {
            double p = Math.min(1 - eps, Math.max(eps, probs[i]));
            int y = actual[i];
            int pred = p >= 0.5 ? 1 : 0;

            if (pred == 1 && y == 1) tp++;
            else if (pred == 1 && y == 0) fp++;
            else if (pred == 0 && y == 0) tn++;
            else fn++;

            logLossSum += -(y * Math.log(p) + (1 - y) * Math.log(1 - p));
            brierSum += (p - y) * (p - y);
        }

        double accuracy = (double) (tp + tn) / n;
        double precision = tp + fp == 0 ? 0.0 : (double) tp / (tp + fp);
        double recall = tp + fn == 0 ? 0.0 : (double) tp / (tp + fn);
        double f1 = precision + recall == 0 ? 0.0
                : 2 * precision * recall / (precision + recall);

        return new Metrics(accuracy, precision, recall, f1,
                logLossSum / n, brierSum / n, n);
    }

    /** Convenience for libraries that hand back List<Double> probabilities. */
    public static Metrics from(List<Double> probs, int[] actual) {
        double[] p = new double[probs.size()];
        for (int i = 0; i < p.length; i++) {
            p[i] = probs.get(i);
        }
        return from(p, actual);
    }

    @Override
    public String toString() {
        return String.format(
                "n=%d  accuracy=%.3f  precision=%.3f  recall=%.3f  f1=%.3f  logLoss=%.4f  brier=%.4f",
                n, accuracy, precision, recall, f1, logLoss, brier);
    }
}
