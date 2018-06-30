package main.java.matching;

import com.graphhopper.util.GPXEntry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import main.java.matching.controller.MatchingService;
import main.java.matching.database.DataRepository;
import main.java.matching.models.XFDEntry;
import main.java.matching.services.PreProcess;
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

    // Error Tax 8% with 50 taxis
    //
    public static void main(String[] args) {
        logger.info("Init Map Matching");

        PreProcess preProcess = new PreProcess();

        DataRepository repository = new DataRepository();

        List<XFDEntry> matchingEntries = new ArrayList<>();

        GeometryFactory geometry = new GeometryFactory(new PrecisionModel(), 4326);

        // Create polygon manually

        // initial lon lat
        // final lon | initial lat
        // final lon lat
        // initial lon | final lat
        // initial lon lat

        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(116.08, 39.68),
                new Coordinate(116.77, 39.68),
                new Coordinate(116.77, 40.18),
                new Coordinate(116.08, 40.18),
                new Coordinate(116.08, 39.68)
        };

        Polygon polygon = geometry.createPolygon(coordinates);

        // Create polygon by OSM_POLY file

        // Polygon polygon = PolyReader.readLimitsOSM(OSM_POLY);

        MatchingService service = new MatchingService();

        service.configMatching(OSM_FILE_PATH, GHLOCATION);

        Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntries(TABLE);

        // Match in GPX entries
        for (Integer entry: gpxEntries.keySet()) {
            List<GPXEntry> gpxUnmatched = gpxEntries.get(entry);

            List<GPXEntry> gpxUnmatchedClean = preProcess.preprocessByOSMLimit(gpxUnmatched, polygon);

            try {
                List<XFDEntry> xfdEntries = service.matchingEntries(gpxUnmatchedClean, entry);

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

        logger.info("Entries matching >> " + matchingEntries.size());

        repository.saveXFDEntries(matchingEntries);

        logger.info("Save in db.");

        logger.info("Finish.");
    }

}
