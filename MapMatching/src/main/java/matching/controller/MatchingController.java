package matching.controller;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.GPXEntry;
import matching.App;
import matching.models.FDEntry;
import matching.models.XFDEntry;
import matching.services.FDMatcher;
import matching.services.GraphHopperMapMatching;
import matching.utils.Calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MatchingController {
    private static final String
            OSM_FILE_PATH = "Beijing.osm.pbf",
            GHLOCATION = "graphopper-beijing";

    private GraphHopperMapMatching mapMatching;

    public MatchingController() {
        mapMatching = new GraphHopperMapMatching(OSM_FILE_PATH, GHLOCATION);
    }

    public List<XFDEntry> matchingEntries(List<GPXEntry> gpxUnmatched, long taxiId) {
        // Convert GPX entries in FD entries
        List<FDEntry> fdUnmatched = Calc.convertGPXEntryInFCDEntry(gpxUnmatched);

        List<FDEntry> fdMatched = mapMatching.doMatchingAndGetFCDEntries(gpxUnmatched);

        // Rematch FD entries
        List<FDEntry> fdMatch = FDMatcher.doFCDMatching(fdUnmatched, fdMatched);

        List<FDEntry> fdEntriesNoGaps = FDMatcher.fillGaps(fdMatch);

        // Alternative: Replace the first and last if I've been in the 1km radius for gpxEntries times.
        if (fdEntriesNoGaps.size() <= 0)
            throw new NullPointerException("Gaps not filled");

        // Remove gaps in FD entries
        long diff = 120; // 2 minutes
        FDMatcher.fillInvalidTimes(fdEntriesNoGaps, diff);

        // Convert in XFD entries
        return fdEntriesNoGaps.stream().map(fdEntry ->
                new XFDEntry(fdEntry, taxiId) // with trajectory id
        ).collect(Collectors.toList());
    }

    public List<GPXEntry> preProcessing(List<GPXEntry> gpxEntries) {
        List<GPXEntry> newEntries = new ArrayList<>();
        int tam = gpxEntries.size();
        double dist, distanceLimit = 20000; // Using 120km/h in 10min -> 20km

        // Doesn't have the amount of data needed
        if (tam <= 2)
            return new ArrayList<>();

        newEntries.add(gpxEntries.get(0));

        for (int i = 1; i < tam; i++) {
            dist = Calc.calcDist(gpxEntries.get(i - 1), gpxEntries.get(i));

            if (dist > distanceLimit)
                break;

            newEntries.add(gpxEntries.get(i));
        }

        return newEntries;
    }

}
