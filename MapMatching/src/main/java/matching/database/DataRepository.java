package matching.database;

import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.shapes.GHPoint;
import matching.App;
import matching.models.XFDEntry;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class DataRepository {
    private Connection connection = null;

    /**
     * Get all positions of the all taxis in determinate day
     *
     * @param tableName: Table name with data about taxis
     * @return Map<Integer, List<GPXEntry> has taxi Id as key and all positions
     * */
    public Map<Integer, List<GPXEntry>> getAllEntries(String tableName) {
        String query = " select taxi_id, date_time, longitude, latitude from " + tableName
                + " WHERE date_time::date >= DATE '2008-02-02' AND date_time::date < DATE '2008-02-03' "
                + " order by date_time ";
        Map<Integer, List<GPXEntry>> trajectories = new HashMap<>();

        int _id;

        try {
            connection = ConnectionFactory.getConnection();

            PreparedStatement statement = connection.prepareStatement(query);

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



        } catch (PropertyVetoException | SQLException | IOException e) {
            App.logger.error("Error in read gpx entries", e);
        }

        return trajectories;
    }

    public void saveXFDEntries (List<XFDEntry> entries) {
        Connection connection = null;
        PreparedStatement stmt = null;
        Integer batchSize = 1000;

        String query = "insert into matched_tracks (tid,latitude,longitude,datetime,edge_id,geom) " +
                "values (?,?,?,?,?,ST_SetSRID(ST_MakePoint(?, ?), 4326));";

        try {
            connection = ConnectionFactory.getConnection();
            connection.setAutoCommit(false);
            stmt = connection.prepareStatement(query);

            for (int i = 0; i < entries.size(); i++) {
                XFDEntry entry = entries.get(i);
                stmt.setInt(1, entry.getTid().intValue());
                stmt.setDouble(2, entry.getLat());
                stmt.setDouble(3, entry.getLon());
                stmt.setTimestamp(4, entry.getTimestamp());
                stmt.setLong(5, entry.getEdgeId().longValue());
                //GEOMETRY
                stmt.setDouble(6, entry.getGeometry().getCentroid().getX()); // Longitude
                stmt.setDouble(7, entry.getGeometry().getCentroid().getY()); // Latitude
                stmt.addBatch();

                if (i % batchSize == 0)
                    stmt.executeBatch();
            }

            // For remaining data
            stmt.executeBatch();

            connection.commit();
        } catch (SQLException | IOException | PropertyVetoException e) {
            App.logger.error("Timestamp invalid!", e);
            try {
                stmt.clearBatch();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
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


//    public void createTableXFCDEntries() {
//        Connection connection = null;
//        try {
//            connection = ConnectionFactory.getConnection();
//        } catch (ClassNotFoundException | SQLException | IOException e) {
//            e.printStackTrace();
//        }
//
//        Statement createSeq = null;
//        Statement createTable = null;
//
//        try {
//            // first create a sequence for the new table
//            createSeq = connection.createStatement();
//            createSeq.executeUpdate("CREATE SEQUENCE matched_tracks_seq");
//            System.out.println("Sequence 'matched_tracks_seq' successfully created.");
//        } catch (SQLException e) {
//            System.out.println("Sequence 'matched_tracks_seq' already exists.");
//        }
//
//        try {
//            // now create the new table
//            createTable = connection.createStatement();
//            StringBuilder table = new StringBuilder()
//                    .append("CREATE TABLE matched_tracks (")
//                    //tid,latitude,longitude,date_time,edge_id,geometry,offset,gid
//                    .append("tid INT,")
//                    .append("latitude DOUBLE PRECISION,")
//                    .append("longitude DOUBLE PRECISION,")
//                    .append("datetime TIMESTAMP WITHOUT TIME ZONE,")
//                    .append("edge_id BIGINT,")
//                    .append("geom GEOMETRY(Point,4326),")
//                    .append("_offset DOUBLE PRECISION,")
//                    .append("gid INT PRIMARY KEY")
//                    .append(");");
//            createTable.executeUpdate(table.toString());
//            System.out.println("Table 'matched_tracks' successfully created.");
//        } catch (SQLException e) {
//            System.out.println("Table 'matched_tracks' already exists.");
//        }
//
//        finally {
//            // closing statements
//            if (createSeq != null) {
//                try {
//                    createSeq.close();
//                } catch (SQLException e) {
//                    //;
//                }
//                createSeq = null;
//            }
//            if (createTable != null) {
//                try {
//                    createTable.close();
//                } catch (SQLException e) {
//                    //;
//                }
//                createTable = null;
//            }
//        }
//    }

}