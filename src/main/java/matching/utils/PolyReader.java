package main.java.matching.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import main.java.matching.App;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PolyReader {

    private static GeometryFactory geoFactory = new GeometryFactory();

    /**
     * Read OSM limits from file 'city_name.poly'
     * DOWN_LEFT, DOWN_RIGHT, UP_RIGHT, UP_LEFT
     * */
    public static Polygon readLimitsOSM(String poly) {
        Polygon polygon = null;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(poly)));
            br.readLine();
            br.readLine();

            String dleft = br.readLine(),
                    dright = br.readLine(),
                    uright = br.readLine(),
                    uleft = br.readLine();

            Coordinate [] coordinates = new Coordinate[5];
            coordinates[0] = getCoordinate(dleft);
            coordinates[1] = getCoordinate(dright);
            coordinates[2] = getCoordinate(uright);
            coordinates[3] = getCoordinate(uleft);
            coordinates[4] = getCoordinate(dleft);

            LinearRing linear = new GeometryFactory().createLinearRing(coordinates);

            polygon = geoFactory.createPolygon(linear);

        } catch (FileNotFoundException e) {
            App.logger.info("File " + poly + " Not Found!");
            e.printStackTrace();
        } catch (IOException e) {
            App.logger.info("File invalid! Error to read data.");
            e.printStackTrace();
        }
        return polygon;
    }

    private static Coordinate getCoordinate(String data) {
        List<String> lon_lat = Arrays.stream(data.replace(" ", ",").split(","))
                .filter(v -> !v.isEmpty()).collect(Collectors.toList());
        return new Coordinate(
                Double.valueOf(lon_lat.get(0)),
                Double.valueOf(lon_lat.get(1))
        );
    }
}
