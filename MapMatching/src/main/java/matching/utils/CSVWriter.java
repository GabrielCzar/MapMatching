package matching.utils;

import matching.models.FCDEntry;
import matching.models.GFCDEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

public class CSVWriter {
    private static String HEADER = "taxi_id, latitude, longitude, ele, date_time";

    private static void writer(String filename, List<String> data) {
        try {
            PrintStream pt = new PrintStream(new FileOutputStream(filename, true));

            pt.println(HEADER);

            for (String s : data)
                pt.println(s);

            System.out.println("Saved " + data.size() + " points.");
        } catch (IOException e) {
            System.out.println("Error Writing GPX" + e);
        }
    }

    public static void writerGFCDEntries(String filename, List<GFCDEntry> points, Integer trajectoryID) {
        HEADER = GFCDEntry.HEADER;
        writer(filename, points.stream().map(entry -> formatGFCDEntry(entry, trajectoryID)).collect(Collectors.toList()));
    }

    private static String formatGFCDEntry(FCDEntry entry, int trajectoryID) {
        return trajectoryID + ", " + entry.toString();
    }
}