import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.MappingIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads results.csv into List<Match> using Jackson's CSV dataformat. Jackson
 * handles quoting, embedded commas, and newlines correctly — the things a naive
 * String.split(",") gets wrong on real data like team and city names.
 */
public class DataLoader {

    /**
     * Reads every row of results.csv. The file ships future fixtures with "NA"
     * scores; withNullValue("NA") turns those into null Integers so they parse
     * cleanly. We keep them — they're separated later (see played/upcoming).
     */
    public static List<Match> loadAll(String path) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema()
                .withHeader()
                .withNullValue("NA");
        try (MappingIterator<Match> it = mapper
                .readerFor(Match.class)
                .with(schema)
                .readValues(new File(path))) {
            List<Match> matches = new ArrayList<>();
            while (it.hasNext()) {
                matches.add(it.next());
            }
            return matches;
        }
    }

    /**
     * Matches with a known result. These are the only ones we can train on or
     * measure accuracy against — supervised learning needs the answer.
     */
    public static List<Match> played(List<Match> all) {
        List<Match> out = new ArrayList<>();
        for (Match m : all) {
            if (m.isPlayed()) {
                out.add(m);
            }
        }
        return out;
    }

    /**
     * Future fixtures with no result yet (NA scores). We don't train on these —
     * but they are exactly what a model is *for*: real upcoming matches to
     * predict. We compute their features from history and let the model call them.
     */
    public static List<Match> upcoming(List<Match> all) {
        List<Match> out = new ArrayList<>();
        for (Match m : all) {
            if (!m.isPlayed()) {
                out.add(m);
            }
        }
        return out;
    }
}
