package matching;

import com.graphhopper.util.GPXEntry;
import matching.controller.MatchingController;
import matching.database.DataRepository;
import matching.models.XFDEntry;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
            logger.error("Error in read gpx entries");
            e.printStackTrace();
        }

        MatchingController controller = new MatchingController();

        // Match in GPX entries
        //for (Integer entry: gpxEntries.keySet()) {
            Integer entry = 35;
            List<GPXEntry> gpxUnmatched = gpxEntries.get(entry);
            logger.info("SIZE UN >> " + gpxUnmatched.size());

            List<GPXEntry> gpxPreProcessing = controller.preProcessing(gpxUnmatched);

            logger.info("SIZE PRE >> " + gpxPreProcessing.size());

    //        if (gpxPreProcessing.size() <= 0)
      //          continue;

            try {
                List<XFDEntry> xfdEntries = controller.matchingEntries(gpxPreProcessing, entry);

                logger.info("XFD SIZE >> " + xfdEntries.size());

          //      repository.saveXFCDEntries(xfdEntries);

            //    logger.info("Saved entries for taxi " + entry);

            } catch (Exception e) {
                logger.error("Error to matching entry " + entry.toString());
                e.printStackTrace();
            }
        //}
        logger.info("FINISH");
    }

}
