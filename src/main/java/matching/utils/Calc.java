package main.java.matching.utils;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.GPXEntry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Calc {

    public static double calcAverageSpeed(double d, long iTime, long fTime){
        return d / ((fTime - iTime) / 1000);
    }

    public static long calcTimeVariation(double d, double vm) {
        return (long) ((d / vm) * 1000);
    }

    public static double calcDist(GPXEntry last, GPXEntry actual) {
        DistanceCalc distanceCalc = new DistancePlaneProjection();
        return distanceCalc.calcDist(
                last.getLat(),
                last.getLon(),
                actual.getLat(),
                actual.getLon()
        );
    }

    public static long calcTimeInterval(long actual, long previous) {
        return (actual - previous) / 1000;
    }

    public static Point convertGpxToPoint(GPXEntry entry) {
        GeometryFactory geoFactory = new GeometryFactory();
        return geoFactory.createPoint(new Coordinate(entry.getLon(), entry.getLat()));
    }

}
