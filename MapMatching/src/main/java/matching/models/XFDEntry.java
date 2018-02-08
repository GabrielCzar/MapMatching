package matching.models;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.sql.Timestamp;

public class XFDEntry extends FDEntry {
    public static final String HEADER = "tid,latitude,longitude,date_time,edge_id,geometry,offset,gid";
    private static GeometryFactory geoFactory = new GeometryFactory();

    private Long tid;
    private Geometry geometry;
    private double offset;
    private int gid;

    public XFDEntry(FDEntry e, Integer gid) {
        super(e.getLat(), e.getLon(), e.ele, e.getTime(), e.getSpeed(), e.getEdgeId());
        this.geometry = calcGeometry(e.getLat(), e.getLon());
        this.offset = 0;
        this.gid = gid;
    }

    public XFDEntry(FDEntry e, Long tid) {
        super(e.getLat(), e.getLon(), e.ele, e.getTime(), e.getSpeed(), e.getEdgeId());
        this.geometry = calcGeometry(e.getLat(), e.getLon());
        this.offset = 0;
        this.tid = tid;
    }

    private Geometry calcGeometry(double lat, double lon) {
        return geoFactory.createPoint(new Coordinate(lon, lat));
    }

    @Override
    public String toString() {
        return super.toString() + ", " + geometry + ", , " + gid;
    }

    public Long getTid() {
        return tid;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public double getOffset() {
        return offset;
    }

    public int getGid() {
        return gid;
    }

    public Timestamp getTimestamp() {
        return new Timestamp(super.getTime());
    }
}
