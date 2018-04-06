package main.java.matching.controller;

import com.graphhopper.util.GPXEntry;
import main.java.matching.models.XFDEntry;
import main.java.matching.services.FDMatcher;
import main.java.matching.services.GraphHopperMapMatching;
import main.java.matching.services.PreProcess;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MatchingController {
    private GraphHopperMapMatching mapMatching;

    public MatchingController(String osmFilePath, String ghLocation) {
	mapMatching = new GraphHopperMapMatching(osmFilePath, ghLocation);
    }

    /**
     * @param gpxEntries will be processed for then be use for GraphHopper/MapMatching that at the moment doesn't have timestamps
     * @return List<XFDEntry> with gaps filled
     * */
    public List<XFDEntry> matchingEntries(List<GPXEntry> gpxEntries, long taxiId) {
        PreProcess pp = new PreProcess();
        HashMap<PreProcess.TYPE_ENTRY, List<?>> preProcessed = pp.preProcessBySpeed(gpxEntries, taxiId);
        List<GPXEntry> gpx = (List<GPXEntry>) preProcessed.get(PreProcess.TYPE_ENTRY.GPX);
        List<XFDEntry> xfd = (List<XFDEntry>) preProcessed.get(PreProcess.TYPE_ENTRY.XFD);

        List<XFDEntry> ghMatched = mapMatching.doMapMatching(gpx, taxiId);

//        List<XFDEntry> xfd = gpxEntries.stream().map(gpxEntry -> new XFDEntry(gpxEntry, taxiId)).collect(Collectors.toList());

        // Rematch FD entries
        List<XFDEntry> fdMatch = FDMatcher.doFCDMatching(xfd, ghMatched);

        return FDMatcher.fillGaps(fdMatch);
    }




}
