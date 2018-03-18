package matching;

import com.graphhopper.util.GPXEntry;
import matching.controller.MatchingController;
import matching.database.DataRepository;
import matching.models.XFDEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    private static final String
            TABLE = "taxi_data",
            filename = "fcd-entries.csv";
    private static final String
            OSM_FILE_PATH = "Beijing.osm.pbf",
            GHLOCATION = "graphopper-beijing";

    public static final Logger logger =
            LoggerFactory.getLogger(App.class);


    // Error Tax 6% with 50 taxis
    //
    public static void main(String[] args) {
        logger.info("Init Map Matching");

        DataRepository repository = new DataRepository();

        Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntries(TABLE);

        MatchingController controller = new MatchingController(OSM_FILE_PATH, GHLOCATION);

        List<XFDEntry> matchingEntries = new ArrayList<>();

        // Match in GPX entries
        for (Integer entry: gpxEntries.keySet()) {
            List<GPXEntry> gpxUnmatched = gpxEntries.get(entry);

            try {
                List<XFDEntry> xfdEntries = controller.matchingEntries(gpxUnmatched, entry);

                if (xfdEntries.size() > 0) {
                    matchingEntries.addAll(xfdEntries);
                    logger.info("Entries for taxi " + entry);
                } else {
                    logger.info("No sufficiently data of taxi " + entry);
                }
            } catch (Exception e) {
                logger.error("\nError to matching entry " + entry.toString());
                e.printStackTrace();
            }
        }
        logger.info("Matching Finish.\nSave result in db.");

        repository.saveXFDEntries(matchingEntries);

        logger.info("FINISH");
    }

}
