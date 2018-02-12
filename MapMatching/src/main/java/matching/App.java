package matching;

import com.graphhopper.util.GPXEntry;
import matching.database.DataRepository;
import matching.models.FDEntry;
import matching.models.XFDEntry;
import matching.services.FDMatcher;
import matching.services.GraphHopperMapMatching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class App {
    private static final String
            TABLE = "taxi_data",
            OSM_FILE_PATH = "Beijing.osm.pbf",
            GHLOCATION = "graphopper-beijing",
            filename = "fcd-entries.csv";

    public static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Init");

        DataRepository repository = new DataRepository();
        try {
            int limit = -1;
            Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntriesAsGPX(TABLE, limit);

            GraphHopperMapMatching mapMatching = new GraphHopperMapMatching(OSM_FILE_PATH, GHLOCATION);

            // Match in GPX entries
            List<GPXEntry> gpxUnmatched = gpxEntries.get(1);

            // Convert GPX entries in FD entries
            List<FDEntry> fdUnmatched = FDMatcher.convertGPXEntryInFCDEntry(gpxUnmatched);
            List<FDEntry> fdMatched = mapMatching.doMatchingAndGetFCDEntries(gpxUnmatched);

            // Rematch FD entries
            List<FDEntry> fdMatch = FDMatcher.doFCDMatching(fdUnmatched, fdMatched);

            List<FDEntry> fdEntriesNoGaps = FDMatcher.fillGaps(fdMatch);

            // Remove gaps in FD entries
            FDMatcher.fillInvalidTimes(fdEntriesNoGaps, 120, mapMatching);

            logger.info("-------- ENTRIES -------");

            // Convert in XFD entries
            //List<XFDEntry> gfdEntries = fdEntriesNoGaps.stream().map(
            //        fdEntry -> {
            //            logger.info(fdEntry.toString());
            //            return new XFDEntry(fdEntry, 1L); // with trajectory id
            //        }
            //).collect(Collectors.toList());



            //repository.createTableXFCDEntries();
            //logger.info("Trying save in database...");
            //repository.saveXFCDEntries(gfdEntries);
            //logger.info("Saved!");

            // Export to CSV
            //CSVWriter.writerGFCDEntries(filename, gfdEntries, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}