package matching.database;

import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.shapes.GHPoint;
import matching.models.XFDEntry;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class DataRepository {

    /**
     * Get all positions of the all taxis in determinate day
     *
     * @param tableName: Table name with data about taxis
     * @return Map<Integer, List<GPXEntry> has taxi Id as key and all positions
     * */
    public Map<Integer, List<GPXEntry>> getAllEntriesAsGPX(String tableName) throws ClassNotFoundException, SQLException, IOException {
        Connection connection = ConnectionFactory.getConnection();

        String query = " select taxi_id, date_time, longitude, latitude from " + tableName
                     + " WHERE date_time::date >= DATE '2008-02-02' AND date_time::date < DATE '2008-02-03' "
                     + " order by date_time ";

        PreparedStatement statement = connection.prepareStatement(query);

        Map<Integer, List<GPXEntry>> trajectories = new HashMap<>();

        int _id;

        ResultSet result = statement.executeQuery();

        while (result.next()) {
            _id = result.getInt("taxi_id");

            if (!trajectories.containsKey(_id))
                trajectories.put(_id, new ArrayList<>());
            if (trajectories.containsKey(_id)) {
                trajectories.get(_id).add(
                        new GPXEntry(
                                new GHPoint(
                                        result.getDouble("latitude"),
                                        result.getDouble("longitude")),
                                        getDateTime(result.getString("date_time")).getTime()
                        )
                );
            }
        }

        connection.close();

        return trajectories;
    }

    public Map<Integer, List<GPXEntry>> getAllEntriesAsGPXFromTaxiID(String tableName, int taxiId, int limit) throws ClassNotFoundException, SQLException, IOException {
        Connection connection = ConnectionFactory.getConnection();

        String limited = limit > 0 ? "limit " + limit : "";
        String query =
                  "select taxi_id, date_time, longitude, latitude from " + tableName
                + " WHERE date_time::date >= DATE '2008-02-02' AND date_time::date < DATE '2008-02-03' "
                + " AND taxi_id = " + taxiId + " order by date_time " + limited;

        PreparedStatement statement = connection.prepareStatement(query);

        Map<Integer, List<GPXEntry>> trajectories = new HashMap<>();
        ArrayList<GPXEntry> entries = new ArrayList<>();

        int _id = -1;

        ResultSet result = statement.executeQuery();

        while (result.next()) {
            if (_id == -1) {
                _id = result.getInt(1);
            } else if (_id != result.getInt(1)) {
                _id = result.getInt(1);
                trajectories.put(_id, entries);
                entries = new ArrayList<>();
            }

            entries.add(
                    new GPXEntry(
                            new GHPoint(
                                    result.getDouble("latitude"),
                                    result.getDouble("longitude")),
                            getDateTime(result.getString("date_time")).getTime()));
        }

        trajectories.put(_id, entries);

        connection.close();

        return trajectories;
    }

    public void createTableXFCDEntries() {
        Connection connection = null;
        try {
            connection = ConnectionFactory.getConnection();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }

        Statement createSeq = null;
        Statement createTable = null;

        try {
            // first create a sequence for the new table
            createSeq = connection.createStatement();
            createSeq.executeUpdate("CREATE SEQUENCE matched_tracks_seq");
            System.out.println("Sequence 'matched_tracks_seq' successfully created.");
        } catch (SQLException e) {
            System.out.println("Sequence 'matched_tracks_seq' already exists.");
        }

        try {
            // now create the new table
            createTable = connection.createStatement();
            StringBuilder table = new StringBuilder()
                    .append("CREATE TABLE matched_tracks (")
                    //tid,latitude,longitude,date_time,edge_id,geometry,offset,gid
                    .append("tid INT,")
                    .append("latitude DOUBLE PRECISION,")
                    .append("longitude DOUBLE PRECISION,")
                    .append("datetime TIMESTAMP WITHOUT TIME ZONE,")
                    .append("edge_id BIGINT,")
                    .append("geom GEOMETRY(Point,4326),")
                    .append("_offset DOUBLE PRECISION,")
                    .append("gid INT PRIMARY KEY")
                    .append(");");
            createTable.executeUpdate(table.toString());
            System.out.println("Table 'matched_tracks' successfully created.");
        } catch (SQLException e) {
            System.out.println("Table 'matched_tracks' already exists.");
        }

        finally {
            // closing statements
            if (createSeq != null) {
                try {
                    createSeq.close();
                } catch (SQLException e) {
                    //;
                }
                createSeq = null;
            }
            if (createTable != null) {
                try {
                    createTable.close();
                } catch (SQLException e) {
                    //;
                }
                createTable = null;
            }
        }
    }

    public void saveXFCDEntries (List<XFDEntry> entries) {
        entries.forEach(entry -> saveXFCDEntry(entry));
    }

    public void saveXFCDEntry(XFDEntry entry) {
        Connection connection = null;
        try {
            connection = ConnectionFactory.getConnection();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }

        String query = "insert into matched_tracks (tid,latitude,longitude,datetime,edge_id,geom) " +
                        "values (?,?,?,?,?,ST_SetSRID(ST_MakePoint(?, ?), 4326));";

        PreparedStatement stmt;

        try {
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, entry.getTid().intValue());
            stmt.setDouble(2, entry.getLat());
            stmt.setDouble(3, entry.getLon());
            stmt.setTimestamp(4, entry.getTimestamp());
            stmt.setLong(5, entry.getEdgeId().longValue());
            //GEOMETRY
            stmt.setDouble(6, entry.getGeometry().getCentroid().getX()); // Longitude
            stmt.setDouble(7, entry.getGeometry().getCentroid().getY()); // Latitude

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private Timestamp getDateTime(String date_time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(date_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Timestamp(parsedDate.getTime());
    }
}