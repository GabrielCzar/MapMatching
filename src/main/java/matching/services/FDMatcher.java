package main.java.matching.services;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import main.java.matching.models.XFDEntry;
import main.java.matching.utils.Calc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class FDMatcher {
    private static double VMMIN = 0.01; // m/s -> 1km 10 days

    //private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static List<XFDEntry> doFCDMatching(List<XFDEntry> fcdUnmatched, List<XFDEntry> fcdMatched) {
        fillFirstAndLastInvalidTimestamps(fcdUnmatched, fcdMatched);

        // init list for query nodes (outer loop)
        ConcurrentSkipListSet<Integer> queryNodes = new ConcurrentSkipListSet<>();
        // init and fill hash map for matching candidates per inner node (inner loop)
        ConcurrentHashMap<Integer, ConcurrentSkipListMap<Double, Integer>> candidateNodes = new ConcurrentHashMap<>();

        DistanceCalc distanceCalc = new DistancePlaneProjection();
        List<XFDEntry> outerLoop = null;
        List<XFDEntry> innerLoop = null;

        // choose smaller point list as outer loop
        if (fcdUnmatched.size() < fcdMatched.size()) {
            outerLoop = fcdUnmatched;
            innerLoop = fcdMatched;
        }
        else {
            outerLoop = fcdMatched;
            innerLoop = fcdUnmatched;
        }

        for (int i=0;i<innerLoop.size();i++)
            candidateNodes.put(i, new ConcurrentSkipListMap<>());

        // loop over smaller point list
        for (int i = 0; i < outerLoop.size();i++) {
            XFDEntry outerNode = outerLoop.get(i);

            // parameters to approach closest node
            double checkDistance = 50.0;
            boolean found = false;
            int tolerance = 5;

            // loop over greater point list
            for (int j=0;j<innerLoop.size();j++) {
                XFDEntry innerNode = innerLoop.get(j);

                // calculate the distance
                double distance = distanceCalc.calcDist(
                        outerNode.lat, outerNode.lon, innerNode.lat, innerNode.lon
                );

                // are we getting closer than the checkDistance?
                if (distance <= checkDistance) {
                    found = true; // yes we are
                    tolerance = 5; // keep tolerance
                    //checkDistance = distance; // update checkDistance
                    candidateNodes.get(j).put(distance, i); // remember node as possible candidate
                }
                else {
                    // if found is true we are moving further away again
                    // but we might get closer again, so we use a tolerance counter
                    tolerance += found ? -1 : 0;
                    if (tolerance < 0)
                        break;
                }
            }
            // add query node if we got a candidate
            if (found)
                queryNodes.add(i);
        }

        // create new map to control if matches are not in mixed order
        ConcurrentSkipListMap<Integer, Integer> matches = new ConcurrentSkipListMap<>();

        // search for the best matches as long as queryNodes are not matched completely
        while (!queryNodes.isEmpty()) {
            for (int i : queryNodes) {
                // at first every query node is a potential candidate
                // but this might change later in the code
                boolean hasCandidates = false;

                // search where the query node is the best candidate for one or multiple inner nodes
                TreeMap<Double,Integer> winnerNodes = new TreeMap<>();
                for (Map.Entry<Integer,ConcurrentSkipListMap<Double,Integer>> entry : candidateNodes.entrySet())
                    if (!entry.getValue().isEmpty()) {
                        // is the query node among the candidates of the inner node?
                        if (entry.getValue().containsValue(i))
                            hasCandidates = true;

                        // pick a "winner" node
                        if (entry.getValue().firstEntry().getValue() == i)
                            winnerNodes.put(entry.getValue().firstKey(), entry.getKey());
                    }

                // if query node is not a candidate for any of the inner nodes (anymore) it will be removed
                if (!hasCandidates) {
                    queryNodes.remove(i);
                    continue;
                }

                // process possible candidates
                if (!winnerNodes.isEmpty()) {
                    for (int innerNodeNo : winnerNodes.values()) {
                        // check if new match is violating the order of inner nodes
                        if (!matches.isEmpty()) {
                            // get the matching inner nodes for lower and upper query nodes
                            int lowerBound = matches.floorEntry(i) == null ? 0 : matches.floorEntry(i).getValue();
                            int upperBound = matches.ceilingEntry(i) == null ? innerNodeNo : matches.ceilingEntry(i).getValue();
                            if (lowerBound > innerNodeNo || upperBound < innerNodeNo) {
                                // match is not possible, so remove relation from candidate nodes
                                candidateNodes.get(innerNodeNo).values().remove(i);
                                continue;
                            }
                        }

                        // it's a match!
                        // copy time and speed between matching points
                        if (outerLoop.get(i).getTime() == 0) {
                            outerLoop.get(i).setTime(innerLoop.get(innerNodeNo).getTime());
                            outerLoop.get(i).setSpeed(innerLoop.get(innerNodeNo).getSpeed());
                        }
                        else {
                            innerLoop.get(innerNodeNo).setTime(outerLoop.get(i).getTime());
                            innerLoop.get(innerNodeNo).setSpeed(outerLoop.get(i).getSpeed());
                        }

                        // list the new match and remove the combo from other elements
                        matches.put(i, innerNodeNo);
                        queryNodes.remove(i);
                        candidateNodes.remove(innerNodeNo);
                        for (Entry<Integer,ConcurrentSkipListMap<Double,Integer>> entry : candidateNodes.entrySet())
                            if (entry.getValue().containsValue(i)) {
                                entry.getValue().values().remove(i);
                            }
                        // stop processing winnerNodes as we already have a match
                        break;
                    }
                }
            }
            // move on if there are still query nodes left
            // some "winner" position might be free now to take over
        }

        return fcdMatched;
    }


    /**
     * If exists invalid times or negative timestamps, they are to be replaced for next valid value
     * Only in the first and/or last value
     * */
    private static void fillFirstAndLastInvalidTimestamps(List<XFDEntry> unmatched, List<XFDEntry> matched) {
        // first value
        if (matched.get(0).getTime() <= 0)
            matched.get(0).setTime(unmatched.get(0).getTime());

        int tamMatched = matched.size();
        int tamUnmatched = unmatched.size();
        // last value
        if (matched.get(tamMatched - 1).getTime() <= 0)
            matched.get(tamMatched - 1).setTime(unmatched.get(tamUnmatched - 1).getTime());
    }

    public static List<XFDEntry> fillGaps(List<XFDEntry> fcdWithGaps) {
        List<XFDEntry> fcdWithoutGaps = new ArrayList<>();
        List<XFDEntry> gap = new ArrayList<>();

        for (int i = 0; i < fcdWithGaps.size(); i++) {
            XFDEntry FDEntry = fcdWithGaps.get(i);
            // extend the gap
            gap.add(FDEntry);

            // if entry has no time set continue with loop
            if (FDEntry.getTime() <= 0)
                continue;
            else {
                // fill the gap if it contains more than one value (must contain zeros then)
                if (gap.size() > 1 && !(gap.size() == 2 && gap.get(0).getTime() > 0))
                    fillGap(gap, fcdWithoutGaps);
                // start new gap
                gap.clear();
                gap.add(FDEntry);
                // add entry to result list
                fcdWithoutGaps.add(FDEntry);
            }
        }

        // there might be a gap left
        if (gap.size() > 1 && !(gap.size() == 2 && gap.get(0).getTime() > 0))
            fillGap(gap, fcdWithoutGaps);

        return fcdWithoutGaps;
    }

    private static void fillGap(List<XFDEntry> gap, List<XFDEntry> fcdWithoutGaps) {
        int tam = gap.size();
        DistanceCalc distanceCalc = new DistancePlaneProjection();
        List<Double> accumulateDistance = new ArrayList<>();
        double distance = 0.0, vm;
        long initialTime = gap.get(0).getTime(),
                finalTime = gap.get(tam - 1).getTime();

        // start accumulative sum with 0.0
        accumulateDistance.add(distance);

        // get the length of the the gap
        for (int i = 1; i < tam; i++) {
            distance += distanceCalc.calcDist(gap.get(i - 1).lat, gap.get(i - 1).lon, gap.get(i).lat, gap.get(i).lon);
            accumulateDistance.add(distance);
        }

        vm = Calc.calcAverageSpeed(distance, initialTime, finalTime);
        if (vm < VMMIN) vm = VMMIN; // supposedly don't exist traffic jam so long

        for (int i = 1; i < tam - 1; i++) {
            long timeVariation = Calc.calcTimeVariation(accumulateDistance.get(i), vm); // milliseconds
            gap.get(i).setTime(initialTime + timeVariation);
            fcdWithoutGaps.add(gap.get(i));
        }
    }

}
