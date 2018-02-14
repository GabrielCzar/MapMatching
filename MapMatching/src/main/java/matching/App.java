package matching;

import com.graphhopper.util.GPXEntry;
import matching.controller.MatchingController;
import matching.database.DataRepository;
import matching.models.XFDEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class App {
    private static final String
            TABLE = "taxi_data",
            filename = "fcd-entries.csv";

    public static final Logger logger =
            LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Init");

        DataRepository repository = new DataRepository();


        Map<Integer, List<GPXEntry>> gpxEntries = null;
        try {
            gpxEntries = repository.getAllEntriesAsGPX(TABLE);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }

        MatchingController controller = new MatchingController();

        // Match in GPX entries
        for (Integer entry: gpxEntries.keySet()) {
            List<GPXEntry> gpxUnmatched = gpxEntries.get(entry);

            try {
                // Miss pre processing of the data
                List<XFDEntry> xfdEntries =
                        controller.matchingEntries(gpxUnmatched, entry);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.info("Matching entries for taxi: ");

            logger.info("Trying save in database...");

            //repository.saveXFCDEntries(xfdEntries);

            logger.info("Saved entries for taxi");
        }

        logger.info("FINISH");


    }

}
