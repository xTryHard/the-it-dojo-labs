import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One row of former_names.csv: a country that played under {@code former}
 * between {@code start} and {@code end}, and is known today as {@code current}.
 * The mapping is date-bounded — a name only resolves within its window.
 */
public record FormerName(
        @JsonProperty("current") String current,
        @JsonProperty("former") String former,
        @JsonProperty("start_date") String start,
        @JsonProperty("end_date") String end) {
}
