import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One row of results.csv. Jackson maps CSV columns into this record by header
 * name. We keep it dumb on purpose: raw fields in, feature engineering happens
 * elsewhere (see FeatureEngineering.java).
 *
 * Scores are Integer (nullable), not int: the file includes future, unplayed
 * fixtures whose scores are the literal string "NA". Those parse to null here
 * and get filtered out in DataLoader — see isPlayed().
 */
public record Match(
        @JsonProperty("date") String date,
        @JsonProperty("home_team") String homeTeam,
        @JsonProperty("away_team") String awayTeam,
        @JsonProperty("home_score") Integer homeScore,
        @JsonProperty("away_score") Integer awayScore,
        @JsonProperty("tournament") String tournament,
        @JsonProperty("city") String city,
        @JsonProperty("country") String country,
        @JsonProperty("neutral") boolean neutral) {

    /** False for future fixtures that haven't been played yet (NA scores). */
    public boolean isPlayed() {
        return homeScore != null && awayScore != null;
    }

    /** The binary target: did the home team win in regulation? */
    public boolean homeWin() {
        return isPlayed() && homeScore > awayScore;
    }
}
