package matching.models;

import com.graphhopper.util.GPXEntry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class XFDEntry extends GPXEntry {
    public static final String HEADER = "tid,latitude,longitude,date_time,edge_id,geometry,offset,gid";
    private static GeometryFactory geoFactory = new GeometryFactory();
    private double offset, speed;
    private BigInteger edgeId;
    private Geometry geometry;
    private Long tid;
    private int gid;


    public XFDEntry(GPXEntry e, Long tid) {
        this(e.lat, e.lon, e.ele, e.getTime(), 0, BigInteger.valueOf(0), tid);
    }

    public XFDEntry(double lat, double lon, double ele, long millis, double speed, BigInteger edgeId, Long tid) {
        super(lat, lon, ele, millis);
        this.speed = speed;
        this.edgeId = edgeId;
        this.geometry = calcGeometry(lat, lon);
        this.offset = 0;
        this.tid = tid;
    }

    /**
     * The speed in kilometers per hour.
     */
    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public BigInteger getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(BigInteger edgeId) {
        this.edgeId = edgeId;
    }

    private Geometry calcGeometry(double lat, double lon) {
        return geoFactory.createPoint(new Coordinate(lon, lat));
    }


    @Override
    public int hashCode() {
        return 59 * super.hashCode() + ((int)speed ^ ((int)speed >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        final XFDEntry other = (XFDEntry) obj;
        return speed == other.speed && super.equals(obj);
    }

    @Override
    public String toString() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.getTime()), ZoneId.of("GMT-3"));
        return this.lat + ", " + this.lon + ", " + ldt + ", " + edgeId + ", " + geometry + ", , " + gid;
    }


    public Long getTid() {
        return tid;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Timestamp getTimestamp() {
        return new Timestamp(super.getTime());
    }
}
