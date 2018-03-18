package matching.controller;

import com.graphhopper.util.GPXEntry;
import matching.models.XFDEntry;
import matching.services.FDMatcher;
import matching.services.GraphHopperMapMatching;
import matching.utils.Calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MatchingController {
    private GraphHopperMapMatching mapMatching;

    public MatchingController(String osmFilePath, String ghLocation) {
	mapMatching = new GraphHopperMapMatching(osmFilePath, ghLocation);
    }

    /**
     * Enum created for optimize pre processing data
     * */
    protected enum TYPE_ENTRY {
        GPX, XFD
    }

    /**
     * @param gpxEntries will be processed for then be use for GraphHopper/MapMatching that at the moment doesn't have timestamps
     * @return List<XFDEntry> with gaps filled
     * */
    public List<XFDEntry> matchingEntries(List<GPXEntry> gpxEntries, long taxiId) {
        HashMap<TYPE_ENTRY, List<?>> preProcessed = preProcessing(gpxEntries, taxiId);
        List<GPXEntry> gpx = (List<GPXEntry>) preProcessed.get(TYPE_ENTRY.GPX);
        List<XFDEntry> xfd = (List<XFDEntry>) preProcessed.get(TYPE_ENTRY.XFD);

        List<XFDEntry> ghMatched = mapMatching.doMapMatching(gpx, taxiId);

        // Rematch FD entries
        List<XFDEntry> fdMatch = FDMatcher.doFCDMatching(xfd, ghMatched);

        return FDMatcher.fillGaps(fdMatch);
    }


    /**
     * @return HashMap<TYPE_ENTRY.GPX, List<GPXEntry>> gpxEntriesPreProcessed
     * @return HashMap<TYPE_ENTRY.XFD, List<XFDEntry>> xfdEntriesPreProcessed
     *
     * Return the same data but with different formats
    * */
    protected HashMap<TYPE_ENTRY, List<?>> preProcessing(List<GPXEntry> unprocessed, Long tid) {
        List<GPXEntry> gpxEntries = new ArrayList<>();
        List<XFDEntry> xfdEntries = new ArrayList<>();

        HashMap<TYPE_ENTRY, List<?>> result = new HashMap<>();
        result.put(TYPE_ENTRY.GPX, gpxEntries);
        result.put(TYPE_ENTRY.XFD, xfdEntries);

        double dist, distanceLimit = 20000; // Using 120km/h in 10min -> 20km
        int tam = unprocessed.size();

        // Doesn't have the amount of data needed
        if (tam <= 2)
            return result;

        gpxEntries.add(unprocessed.get(0));
        xfdEntries.add(new XFDEntry(unprocessed.get(0), tid));

        for (int i = 1; i < tam; i++) {
            dist = Calc.calcDist(unprocessed.get(i - 1), unprocessed.get(i));

            if (dist > distanceLimit) break;

            gpxEntries.add(unprocessed.get(i));
            xfdEntries.add(new XFDEntry(unprocessed.get(i), tid));
        }

        return result;
    }

}
