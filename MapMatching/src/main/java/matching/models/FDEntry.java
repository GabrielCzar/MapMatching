package matching.models;

import com.graphhopper.util.GPXEntry;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FDEntry extends GPXEntry {
    public static final String HEADER = "tid,latitude,longitude,date_time,edgeId";

    private double speed;
    private BigInteger edgeId;

    public FDEntry(GPXEntry e) {
        this(e.lat, e.lon, e.ele, e.getTime(), 0, BigInteger.valueOf(0));
    }

    public FDEntry(double lat, double lon, double ele, long millis, double speed, BigInteger edgeId) {
        super(lat, lon, ele, millis);
        this.speed = speed;
        this.edgeId = edgeId;
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

    @Override
    public int hashCode() {
        return 59 * super.hashCode() + ((int)speed ^ ((int)speed >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        final FDEntry other = (FDEntry) obj;
        return speed == other.speed && super.equals(obj);
    }

    @Override
    public String toString() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(this.getTime()), ZoneId.of("GMT+8"));
        return this.lat + ", " + this.lon + ", " + ldt + ", " + edgeId;
    }
}
