package util;

import java.io.*;
import java.util.List;

public class CSVWriter {
    public static void writeAll(String filePath, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + filePath + " -> " + e.getMessage());
        }
    }

    public static void appendLine(String filePath, String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending file: " + filePath + " -> " + e.getMessage());
        }
    }
}
