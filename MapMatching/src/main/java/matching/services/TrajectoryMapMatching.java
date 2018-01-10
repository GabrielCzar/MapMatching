package matching.services;

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
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import matching.models.FCDEntry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryMapMatching {
    private final String algorithm = Parameters.Algorithms.DIJKSTRA_BI;
    private AlgorithmOptions algorithmOptions;
    private CarFlagEncoder encoder;
    private Weighting weighting;
    private GraphHopper hopper;

    public TrajectoryMapMatching(String osmFilePath, String graphHopperLocation) {
        hopper = new GraphHopperOSM();
        //hopper = new GraphHopper();
        hopper.setDataReaderFile(osmFilePath);
        hopper.setGraphHopperLocation(graphHopperLocation);
        encoder = new CarFlagEncoder();
        hopper.setEncodingManager(new EncodingManager(encoder));
        hopper.getCHFactoryDecorator().setEnabled(false);
        hopper.importOrLoad();

        weighting = new FastestWeighting(encoder);
        algorithmOptions = new AlgorithmOptions(algorithm, weighting);
    }

    public List<GPXEntry> doMatchingAndGetGPXEntries(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algorithmOptions);
        mapMatching.setMeasurementErrorSigma(50);
        MatchResult mr = null;
        try {
            mr = mapMatching.doWork(entries);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<GPXEntry> gpxMatched = new ArrayList<>();

        //System.out.println("Speed - EdgeMatches -> " + weighting.getFlagEncoder().getSpeed(mr.getEdgeMatches().get(0).getEdgeState().getFlags()));
        // Get points of matched track
        Path path = mapMatching.calcPath(mr);
        PointList points = path.calcPoints();

        if (points != null && !points.isEmpty()) {
            for (GHPoint pt : points) {
                //System.out.println(pt);
                gpxMatched.add(new GPXEntry(pt.getLat(), pt.getLon(), 0.0, 0));
            }
        }
        return gpxMatched;
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


    public List<FCDEntry> doMatchingAndGetFCDEntries(List<GPXEntry> entries) {
        MapMatching mapMatching = new MapMatching(hopper, algorithmOptions);
        mapMatching.setMeasurementErrorSigma(50);
        MatchResult mr = null;
        try {
            mr = mapMatching.doWork(entries);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        List<FCDEntry> gpxMatched = new ArrayList<>();

        double speed = weighting.getFlagEncoder().getSpeed(mr.getEdgeMatches().get(0).getEdgeState().getFlags());

        //long millis = mr.getMatchMillis();

        // Get points of matched track
        Path path = mapMatching.calcPath(mr);
        //PointList points = path.calcPoints();

        List<EdgeIteratorState> edges = path.calcEdges();
        edges.forEach(edge ->
            edge.fetchWayGeometry(3).forEach(point ->
                gpxMatched.add(
                        new FCDEntry(point.getLat(), point.getLon(), 0.0, 0, speed, edge.getEdge())
                )
            )
        );


        //if (points != null && !points.isEmpty()) {
        //    for (GHPoint pt : points) {
                //System.out.println(pt);
        //        gpxMatched.add(new FCDEntry(pt.getLat(), pt.getLon(), 0.0, 0, speed));
        //    }
        //}
        return gpxMatched;
    }

    public double getSpeed (long flags) {
        return weighting.getFlagEncoder().getSpeed(flags);
    }
}