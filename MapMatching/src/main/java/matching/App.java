package matching;

import com.graphhopper.util.GPXEntry;
import matching.models.FCDEntry;
import matching.models.XFCDEntry;
import matching.repositories.DataRepository;
import matching.services.FCDMatcher;
import matching.services.TrajectoryMapMatching;
import matching.utils.CSVWriter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class App {
    private static final String TABLE = "taxi_data",
            OSM_FILE_PATH = "Beijing.osm.pbf",
            GHLOCATION = "graphopper-beijing",
            filename = "fcd-entries.csv";

    public static void main(String[] args) {
        DataRepository repository = new DataRepository();
        try {
            int limit = -1;
            Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntriesAsGPX(TABLE, limit);

            TrajectoryMapMatching mapMatching = new TrajectoryMapMatching(OSM_FILE_PATH, GHLOCATION);

            // Match in GPX entries
            List<GPXEntry> gpxUnmatched = gpxEntries.get(1);

            // Convert GPX entries in FCD entries
            List<FCDEntry> fcdUnmatched = FCDMatcher.convertGPXEntryInFCDEntry(gpxUnmatched);
            List<FCDEntry> fcdMatched = mapMatching.doMatchingAndGetFCDEntries(gpxUnmatched);

            // Rematch FCD entries
            List<FCDEntry> fcdMatch = FCDMatcher.doFCDMatching(fcdUnmatched, fcdMatched);

            List<FCDEntry> fcdEntriesNoGaps = FCDMatcher.fillGaps(fcdMatch);

            // Remove gaps in FCD entries
            FCDMatcher.fillInvalidTimesByAvg(fcdEntriesNoGaps);

            // Convert in XFCD entries
            List<XFCDEntry> gfcdEntries = fcdEntriesNoGaps.stream().map(
                    fcdEntry -> new XFCDEntry(fcdEntry, fcdEntriesNoGaps.indexOf(fcdEntry))
            ).collect(Collectors.toList());

            // Export to CSV
            CSVWriter.writerGFCDEntries(filename, gfcdEntries, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}