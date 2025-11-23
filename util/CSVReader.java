package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static List<String> readAll(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath + " -> " + e.getMessage());
        }
        return lines;
    }
}
