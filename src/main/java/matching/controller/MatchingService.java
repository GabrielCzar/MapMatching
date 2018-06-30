package main.java.matching.controller;

import com.graphhopper.util.GPXEntry;
import main.java.matching.App;
import main.java.matching.models.XFDEntry;
import main.java.matching.services.FDMatcher;
import main.java.matching.services.GraphHopperMapMatching;
import main.java.matching.services.PreProcess;

import java.util.HashMap;
import java.util.List;

public class MatchingService {
    private GraphHopperMapMatching mapMatching;

    public MatchingService() { }

    public void configMatching(String osmFilePath, String ghLocation) {
        mapMatching = new GraphHopperMapMatching(osmFilePath, ghLocation);
    }

    /**
     * @param gpxEntries will be processed for then be use for GraphHopper/MapMatching that at the moment doesn't have timestamps
     * @return List<XFDEntry> with gaps filled
     * */
    @SuppressWarnings("unchecked")
    public List<XFDEntry> matchingEntries(List<GPXEntry> gpxEntries, long taxiId) {
        if (mapMatching == null) {
            App.logger.error("Matching doesn't configured");
            App.logger.info("Configure the matcher");
            return null;
        }

        PreProcess pp = new PreProcess();
        HashMap<PreProcess.TYPE_ENTRY, List<?>> preProcessed = pp.preProcessBySpeed(gpxEntries, taxiId);

        List<GPXEntry> gpx = (List<GPXEntry>) preProcessed.get(PreProcess.TYPE_ENTRY.GPX);
        List<XFDEntry> xfd = (List<XFDEntry>) preProcessed.get(PreProcess.TYPE_ENTRY.XFD);

        App.logger.info("preprocessed >> " + gpx.size());

        List<XFDEntry> ghMatched = mapMatching.doMapMatching(gpx, taxiId);

        // Rematch FD entries
        List<XFDEntry> fdMatch = FDMatcher.doFCDMatching(xfd, ghMatched);

        return FDMatcher.fillGaps(fdMatch);
    }




}
