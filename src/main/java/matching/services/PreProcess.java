package main.java.matching.services;

import com.graphhopper.util.GPXEntry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import main.java.matching.App;
import main.java.matching.models.XFDEntry;
import main.java.matching.utils.Calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PreProcess {


    /**
     * Enum created for optimize pre processing data
     * */
    public enum TYPE_ENTRY {
        GPX, XFD
    }

    /**
     * @return HashMap<TYPE_ENTRY.GPX, List<GPXEntry>> gpxEntriesPreProcessed
     * @return HashMap<TYPE_ENTRY.XFD, List<XFDEntry>> xfdEntriesPreProcessed
     *
     * Return the same data but with different formats
     * */
    public HashMap<TYPE_ENTRY, List<?>> preProcessing(List<GPXEntry> unprocessed, Long tid) {
        List<GPXEntry> gpxEntries = new ArrayList<>();
        List<XFDEntry> xfdEntries = new ArrayList<>();

        HashMap<TYPE_ENTRY, List<?>> result = new HashMap<>();
        result.put(TYPE_ENTRY.GPX, gpxEntries);
        result.put(TYPE_ENTRY.XFD, xfdEntries);

        double dist, distanceLimit = 24.9; // ~150km/h meters
        int tam = unprocessed.size();
        int init = 0;

        // Doesn't have the amount of data needed
        if (tam <= 2)
            return result;

        dist = Calc.calcDist(unprocessed.get(0), unprocessed.get(1));

        if (dist < distanceLimit) {
            gpxEntries.add(unprocessed.get(0));
            xfdEntries.add(new XFDEntry(unprocessed.get(0), tid));
        } else {
            init = 1;
            gpxEntries.add(unprocessed.get(1));
            xfdEntries.add(new XFDEntry(unprocessed.get(1), tid));
        }

        for (int i = init + 1; i < tam; i++) {
            dist = Calc.calcDist(unprocessed.get(i - 1), unprocessed.get(i));

            App.logger.info("" + dist);

            if (dist > distanceLimit) break;

            gpxEntries.add(unprocessed.get(i));
            xfdEntries.add(new XFDEntry(unprocessed.get(i), tid));
        }

        return result;
    }

    public HashMap<TYPE_ENTRY, List<?>> preProcessBySpeed(List<GPXEntry> unprocessed, Long tid) {
        List<GPXEntry> gpxEntries = new ArrayList<>();
        List<XFDEntry> xfdEntries = new ArrayList<>();

        HashMap<TYPE_ENTRY, List<?>> result = new HashMap<>();
        result.put(TYPE_ENTRY.GPX, gpxEntries);
        result.put(TYPE_ENTRY.XFD, xfdEntries);

        double dist, vm, speedLimit = 41.6; // 120~150km/h meters 33.4~41.6 m/s
        GPXEntry lastEntry = null, entry;
        boolean broken = false;
        int tam = unprocessed.size();
        long diff;

        // Doesn't have the amount of data needed
        if (tam <= 2)
            return new HashMap<>();

        for (int i = 1; i < tam; i++) {
            if (!broken) {
                lastEntry = unprocessed.get(i - 1);
            } else {
                broken = false;
            }

            entry = unprocessed.get(i);

            dist = Calc.calcDist(lastEntry, entry);
            diff = (entry.getTime() - lastEntry.getTime()) / 1000;
            vm = dist / diff;

            if (vm > speedLimit) {
                broken = true;
            } else {
                gpxEntries.add(entry);
                xfdEntries.add(new XFDEntry(entry, tid));
            }

        }

        return result;
    }

    private GeometryFactory geoFactory = new GeometryFactory();

    public List<GPXEntry> preprocessByOSMLimit(List<GPXEntry> entries, Polygon polygon) {
        List<GPXEntry> result = new ArrayList<>();

        for (GPXEntry entry: entries)
            if (Calc.convertGpxToPoint(entry).within(polygon))
                result.add(entry);

        return result;
    }
}
