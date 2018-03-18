package main.java.matching.utils;

import main.java.matching.models.XFDEntry;

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

    public static void writerGFCDEntries(String filename, List<XFDEntry> points, Integer trajectoryID) {
        HEADER = XFDEntry.HEADER;
        writer(filename, points.stream().map(entry -> formatXFDEntry(entry, trajectoryID)).collect(Collectors.toList()));
    }

    private static String formatXFDEntry(XFDEntry entry, int trajectoryID) {
        return trajectoryID + ", " + entry.toString();
    }
}