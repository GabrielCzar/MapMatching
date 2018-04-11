package test.java.matching;

import com.graphhopper.util.GPXEntry;
import com.vividsolutions.jts.geom.Polygon;
import main.java.matching.App;
import main.java.matching.controller.MatchingController;
import main.java.matching.database.DataRepository;
import main.java.matching.models.XFDEntry;
import main.java.matching.services.PreProcess;
import main.java.matching.utils.PolyReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {
    private static final String TABLE = "taxi_data";
    private static final String
            OSM_FILE_PATH = "Beijing.osm.pbf",
            GHLOCATION = "graphopper-beijing",
            OSM_POLY = "Beijing.poly";

    public static void main(String[] args) {
        App.logger.info("Init Map Matching");


        DataRepository repository = new DataRepository();

        Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntries(TABLE);

        MatchingController controller = new MatchingController(OSM_FILE_PATH, GHLOCATION);

        List<XFDEntry> matchingEntries = new ArrayList<>();

        Integer entry = 1;

        List<GPXEntry> gpxUnmatched = gpxEntries.get(entry);

        App.logger.info("all >> " + gpxUnmatched.size());

        Polygon polygon = PolyReader.readLimitsOSM(OSM_POLY);

        PreProcess preProcess = new PreProcess();

        List<GPXEntry> gpxPreprocessed = preProcess.preprocessByOSMLimit(gpxUnmatched, polygon);

        App.logger.info("osm_limit >> " + gpxPreprocessed.size());

        try {
            List<XFDEntry> xfdEntries = controller.matchingEntries(gpxPreprocessed, entry);

            if (xfdEntries.size() > 0) {

                matchingEntries.addAll(xfdEntries);
                App.logger.info("Entries for taxi " + entry);

            } else {
                App.logger.info("No sufficiently data of taxi " + entry);
            }
        } catch (Exception e) {
            App.logger.error("\nError to matching entry " + entry.toString());
            e.printStackTrace();
        }

        App.logger.info("matching >> " + matchingEntries.size());

        for (int i = 0; i < 50; i++) {
            App.logger.info(matchingEntries.get(i).toString());
        }

        App.logger.info("Finish.");

    }

}
