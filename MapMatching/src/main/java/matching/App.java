package matching;

import com.graphhopper.util.GPXEntry;
import matching.controller.MatchingController;
import matching.database.DataRepository;
import matching.models.FDEntry;
import matching.models.XFDEntry;
import matching.services.FDMatcher;
import matching.services.GraphHopperMapMatching;
import matching.utils.Calc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class App {
    private static final String
            TABLE = "taxi_data",
            filename = "fcd-entries.csv";

    public static final Logger logger =
            LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Init");

        DataRepository repository = new DataRepository();
        try {
            int limit = -1;
            Map<Integer, List<GPXEntry>> gpxEntries =
                    repository.getAllEntriesAsGPX(TABLE, limit);

            // Match in GPX entries
            List<GPXEntry> gpxUnmatched = gpxEntries.get(1);
            logger.info("Read entries");

            MatchingController controller = new MatchingController();
            logger.info("Create GraphHopper instance");

            List<XFDEntry> xfdEntries =
                    controller.matchingEntries(gpxUnmatched);
            logger.info("Matching entries");

            //repository.createTableXFCDEntries();
            logger.info("Trying save in database...");

            repository.saveXFCDEntries(xfdEntries);

            logger.info("Saved!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}