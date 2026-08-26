import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.MappingIterator;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Date-aware team-name canonicalizer, built from former_names.csv.
 *
 * Why this exists: to build a continuous strength rating (Elo) for a country,
 * we replay its matches in order. But a country that changed names appears under
 * both — "Czechoslovakia", "Dahomey", "Netherlands Antilles" — and to a naive
 * replay that looks like a brand-new team with no history, resetting its rating.
 *
 * canonical("Dahomey", 1970-01-01) -> "Benin". Outside the mapped window, or for
 * a name with no entry, the name passes through unchanged.
 */
public class TeamNames {

    private final List<FormerName> mappings;

    private TeamNames(List<FormerName> mappings) {
        this.mappings = mappings;
    }

    public static TeamNames load(String path) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        try (MappingIterator<FormerName> it = mapper
                .readerFor(FormerName.class)
                .with(schema)
                .readValues(new File(path))) {
            List<FormerName> rows = new ArrayList<>();
            while (it.hasNext()) {
                rows.add(it.next());
            }
            return new TeamNames(rows);
        }
    }

    /** Resolve a team name as it stood on a given match date. */
    public String canonical(String team, LocalDate date) {
        for (FormerName m : mappings) {
            if (m.former().equals(team)) {
                LocalDate start = LocalDate.parse(m.start());
                LocalDate end = LocalDate.parse(m.end());
                if (!date.isBefore(start) && !date.isAfter(end)) {
                    return m.current();
                }
            }
        }
        return team;
    }
}
