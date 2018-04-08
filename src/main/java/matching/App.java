package main.java.matching;

import com.graphhopper.util.GPXEntry;
import com.vividsolutions.jts.geom.Polygon;
import main.java.matching.controller.MatchingController;
import main.java.matching.database.DataRepository;
import main.java.matching.models.XFDEntry;
import main.java.matching.services.PreProcess;
import main.java.matching.utils.PolyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    private static final String TABLE = "taxi_data";
    private static final String
            OSM_FILE_PATH = "Beijing.osm.pbf",
            GHLOCATION = "graphopper-beijing",
            OSM_POLY = "Beijing.poly";

    public static final Logger logger = LoggerFactory.getLogger(App.class);

    // Error Tax 6% with 50 taxis
    //
    public static void main(String[] args) {
        logger.info("Init Map Matching");

        PreProcess preProcess = new PreProcess();

        DataRepository repository = new DataRepository();

        List<XFDEntry> matchingEntries = new ArrayList<>();

        Polygon polygon = PolyReader.readLimitsOSM(OSM_POLY);

        MatchingController controller = new MatchingController(OSM_FILE_PATH, GHLOCATION);

        Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntries(TABLE);

        // Match in GPX entries
        for (Integer entry: gpxEntries.keySet()) {
            List<GPXEntry> gpxUnmatched = gpxEntries.get(entry);

            List<GPXEntry> gpxUnmatchedClean = preProcess.preprocessByOSMLimit(gpxUnmatched, polygon);

            try {
                List<XFDEntry> xfdEntries = controller.matchingEntries(gpxUnmatchedClean, entry);

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

        logger.info("Matching Finish.");

//        // Convert in GPX entries
//        @SuppressWarnings("unchecked")
//        List<GPXEntry> entryList = (List<GPXEntry>) (List<? extends GPXEntry>) matchingEntries;

        repository.saveXFDEntries(matchingEntries);

        logger.info("Save in db.");

        logger.info("Finish.");
    }

}
