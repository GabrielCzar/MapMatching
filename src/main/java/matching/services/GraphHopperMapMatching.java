package main.java.matching.services;

import com.graphhopper.GraphHopper;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Parameters;
import main.java.matching.models.XFDEntry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class GraphHopperMapMatching {
    private final String algorithm = Parameters.Algorithms.DIJKSTRA_BI;
    private AlgorithmOptions algorithmOptions;
    private CarFlagEncoder encoder;
    private Weighting weighting;
    private GraphHopper hopper;

    public GraphHopperMapMatching(String osmFilePath, String graphHopperLocation) {
        hopper = new GraphHopperOSM();
        hopper.setDataReaderFile(osmFilePath);
        hopper.setGraphHopperLocation(graphHopperLocation);
        encoder = new CarFlagEncoder();
        hopper.setEncodingManager(new EncodingManager(encoder));
        hopper.getCHFactoryDecorator().setEnabled(false);
        hopper.importOrLoad();

        weighting = new FastestWeighting(encoder);
        algorithmOptions = new AlgorithmOptions(algorithm, weighting);
    }

    public MatchResult doMatching(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algorithmOptions);
        mapMatching.setMeasurementErrorSigma(50);
        MatchResult mr = null;
        try {
            mr = mapMatching.doWork(entries);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mr;
    }

    public List<XFDEntry> doMapMatching(List<GPXEntry> entries, Long tid) {
        MapMatching mapMatching = new MapMatching(hopper, algorithmOptions);
        MatchResult mr = doMatching(entries);

        List<XFDEntry> gpxMatched = new ArrayList<>();

        // Get points of matched track
        Path path = mapMatching.calcPath(mr);

        List<EdgeIteratorState> edges = path.calcEdges();
        edges.forEach(edge ->
                // 2 don't include towers node
                edge.fetchWayGeometry(2).forEach(point ->
                        gpxMatched.add(
                                new XFDEntry(point.getLat(), point.getLon(), 0.0, 0, getSpeed(edge.getFlags()), BigInteger.valueOf(edge.getEdge()), tid)
                        )
                )
        );

        return gpxMatched;
    }

    private double getSpeed (long flags) {
        return weighting.getFlagEncoder().getSpeed(flags);
    }

}