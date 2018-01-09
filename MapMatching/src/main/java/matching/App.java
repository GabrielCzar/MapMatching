package matching;

import com.graphhopper.util.GPXEntry;
import matching.models.FCDEntry;
import matching.repositories.DataRepository;
import matching.services.FCDMatcher;
import matching.services.TrajectoryMapMatching;
import matching.utils.CSVWriter;

import java.util.List;
import java.util.Map;

public class App {
    private static final String TABLE = "taxi_data",
            OSM_FILE_PATH = "Beijing.osm.pbf",
            GHLOCATION = "graphopper-beijing";

    public static void main(String[] args) {
        DataRepository repository = new DataRepository();
        try {
            int limit = 140;
            Map<Integer, List<GPXEntry>> gpxEntries = repository.getAllEntriesAsGPX(TABLE, limit);

            TrajectoryMapMatching mapMatching = new TrajectoryMapMatching(OSM_FILE_PATH, GHLOCATION);

            // Match in GPX entries
            List<GPXEntry> gpxUnmatched = gpxEntries.get(1);

            //CSVWriter.writerGPXEntries("unmatched-points.csv", gpxUnmatched, 1368);

            //MatchResult mr = mapMatching.doMatching(gpxUnmatched);
            ///Map<Integer, SpeedMatch> estimateSpeed = EstimatedSpeedAndTime.estimateSpeed(mr.getEdgeMatches());

            //CSVWriter.writeSpeedMatch("estimated-speed.csv", estimateSpeed, 1368);

            /** CSV Files **/
            // Personalize Matcher
            useFCDEntries(gpxEntries.get(1), mapMatching); // FCDMatcher

            // defaultGPXEntries(gpxUnmatched, mapMatching); // GraphHopper Matcher

            /** GPX Files **/

            //List<GPXEntry> gpxMatched = mapMatching.doMatching(gpxUnmatched);
            //GPXWriter.writer("gpx-matched.gpx", gpxMatched);

            //GPXWriter.writer("gpx-unmatched.gpx", gpxUnmatched);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private static void personalizeMatched(List<GPXEntry> gpxUnmatched, TrajectoryMapMatching mapMatching) {
//        MatchResult mr = mapMatching.doMatching(gpxUnmatched);
//        Map<Integer, SpeedMatch> estimateSpeed = EstimatedSpeedAndTime.estimateSpeed(mr.getEdgeMatches());
//        List<GPXEntry> entrieEstimatedSpeed = new ArrayList<>();
//        for (Map.Entry<Integer, SpeedMatch> entry : estimateSpeed.entrySet()) {
//            SpeedMatch speedMatch = entry.getValue();
//            List<GPXExtension> extensions = speedMatch.edgeMatch.getGpxExtensions();
//            List<GPXEntry> entries = extensions.stream().map(gpxExtension -> gpxExtension.getEntry()).collect(Collectors.toList());
//            entrieEstimatedSpeed.addAll(entries);
//        }
//
//        CSVWriter.writerGPXEntries("map-matching-estimated-speed-gpx-entries.csv", entrieEstimatedSpeed,1368);
//    }

    private static void useFCDEntries (List<GPXEntry> gpxUnmatched, TrajectoryMapMatching mapMatching) {
        int max = 5;
        // Convert GPX entries in FCD entries
        List<FCDEntry> fcdUnmatched = FCDMatcher.convertGPXEntryInFCDEntry(gpxUnmatched);
        List<FCDEntry> fcdMatched = mapMatching.doMatchingAndGetFCDEntries(gpxUnmatched);

        // Rematch FCD entries
        List<FCDEntry> fcdMatch = FCDMatcher.doFCDMatching(fcdUnmatched, fcdMatched);

        System.out.println("FCD MAT\n\n");
        int qtd = 0;

        for (int i = 0; i < fcdMatch.size(); i++) if (fcdMatch.get(i).getTime() > 0) qtd ++;

        System.out.println(fcdMatch.size() + " - "  + qtd + " Matched with data set");

        // double speed = fcdMatch.get(0).getSpeed();

        // Remove gaps in FCD entries
        List<FCDEntry> fcdEntriesNoGaps = FCDMatcher.fillGaps(fcdMatch);


        FCDMatcher.fillInvalidTimesByAvg(fcdEntriesNoGaps); // ERROR


        // Export to CSV

        String filename = "fcd-entries.csv";
        CSVWriter.writerFCDEntries(filename, fcdEntriesNoGaps, 1);
    }

//    private static void defaultGPXEntries (List<GPXEntry> gpxEntries, TrajectoryMapMatching mapMatching) {
//        List<GPXEntry> gpxMatched = mapMatching.doMatchingAndGetGPXEntries(gpxEntries);
//        CSVWriter.writerGPXEntries("map-matching-gpx-entries.csv", gpxMatched,1368);
//    }
//

}