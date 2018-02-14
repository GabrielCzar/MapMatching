package matching.controller;

import com.graphhopper.util.GPXEntry;
import matching.models.FDEntry;
import matching.models.XFDEntry;
import matching.services.FDMatcher;
import matching.services.GraphHopperMapMatching;
import matching.utils.Calc;

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

        // Temporary solution
        if (fdUnmatched.size() <= 2)
            return fdUnmatched.stream().map(fdEntry -> new XFDEntry(fdEntry, taxiId)).collect(Collectors.toList());

        List<FDEntry> fdMatched = mapMatching.doMatchingAndGetFCDEntries(gpxUnmatched);

        // Rematch FD entries
        List<FDEntry> fdMatch = FDMatcher.doFCDMatching(fdUnmatched, fdMatched);

        List<FDEntry> fdEntriesNoGaps = FDMatcher.fillGaps(fdMatch);

        // Remove gaps in FD entries
        long diff = 120; // 2 minutes
        FDMatcher.fillInvalidTimes(fdEntriesNoGaps, diff);

        // Convert in XFD entries
        return fdEntriesNoGaps.stream().map(fdEntry ->
                new XFDEntry(fdEntry, taxiId) // with trajectory id
        ).collect(Collectors.toList());
    }

    public List<GPXEntry> preProcessing(List<GPXEntry> gpxEntries) {
        return null;
    }

}
