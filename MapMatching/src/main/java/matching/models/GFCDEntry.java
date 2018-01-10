package matching.models;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GFCDEntry extends FCDEntry{
    public static final String HEADER = "tid,latitude,longitude,date_time,edge_id,geometry,offset,gid";
    private static GeometryFactory geoFactory = new GeometryFactory();

    private Geometry geometry;
    private double offset;
    private int gid;

    public GFCDEntry(FCDEntry fcdEntry, Integer gid) {
        super(fcdEntry);
        this.geometry = calcGeometry(fcdEntry.getLat(), fcdEntry.getLon());
        this.offset = 0;
        this.gid = gid;
    }

    private Geometry calcGeometry(double lat, double lon) {
        return geoFactory.createPoint(new Coordinate(lon, lat));
    }

    @Override
    public String toString() {
        return super.toString() + ", " + geometry + ", " + offset + ", " + gid;
    }
}
