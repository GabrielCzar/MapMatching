package matching.utils;

import com.graphhopper.util.GPXEntry;
import matching.models.FCDEntry;
import matching.models.SpeedMatch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSVWriter {
    private static String HEADER = "taxi_id, latitude, longitude, ele, date_time";

    public static void writerGPXEntries (String filename, List<GPXEntry> points, Integer trajectoryID) {
        writer(filename, points.stream().map(entry -> formatGpxEntryDefault(entry, trajectoryID)).collect(Collectors.toList()));
    }

    public static void writerFCDEntries (String filename, List<FCDEntry> points, Integer trajectoryID) {
        HEADER = FCDEntry.HEADER;
        writer(filename, points.stream().map(entry -> formatFCDEntry(entry, trajectoryID)).collect(Collectors.toList()));
    }

    private static void writer (String filename, List<String> data) {
        try {
            PrintStream pt = new PrintStream(new FileOutputStream(filename, true));

            pt.println(HEADER);

            for (String s : data)
                pt.println(s);

            System.out.println("Saved " + data.size() + " points.");
        } catch (IOException e) {
            System.out.println("Error Writting GPX"  + e);
        }
    }

    private static String formatGpxEntry (GPXEntry entry, int trajectoryID) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        return trajectoryID + ", " + entry.getLat() + ", " + entry.getLon() + ", " + entry.getEle() + ", " + df.format(new java.sql.Date(entry.getTime()));
    }

    private static String formatGpxEntryDefault (GPXEntry entry, int trajectoryID) {
        return trajectoryID + ", " + entry.toString();
    }


    private static String formatFCDEntry (FCDEntry entry, int trajectoryID) {
        return trajectoryID + ", " + entry.toString();
    }

    public static void writeSpeedMatch(String filename, Map<Integer, SpeedMatch> mapLinkToSpeed, Integer trajectoryID) {
        List<String> list = new ArrayList<>();

        for (Map.Entry<Integer, SpeedMatch> e : mapLinkToSpeed.entrySet())
            list.add(formatSpeedMatch(e.getValue(), trajectoryID));

        writer(filename, list);
    }

    private static String formatSpeedMatch (SpeedMatch speedMatch, Integer trajectoryID) {
        return trajectoryID + "," + speedMatch.edgeMatch.getEdgeState().getEdge() + ","
                + (int) (speedMatch.timestamp / 1000) + "," + speedMatch.speed;
    }
}