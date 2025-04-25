package edu.upc.essi.dtim.NextiaJD.utils;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {
    // Total number of values of the row (i.e. NOT NULL values)
    public static Double getNumberOfValues(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(\"" + column + "\") FROM \"" + tableName + "\"");
        rs.next();
        return rs.getDouble(1);
    }

    // Trim (remove spaces at either side of the string) and lowercase (transform all characters to lowercase)
    public static void preprocessing(Connection conn, String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("DESCRIBE \"" + tableName + "\"");
        List<String> columns = new LinkedList<>();
        while (rs.next()) {
            columns.add(rs.getString(1));
        }
        for (String column: columns) {
            stmt.execute(
                    "UPDATE \"" + tableName + "\" " +
                            "SET \"" + column + "\" = " +
                            "CASE " +
                            "WHEN \"" + column + "\" IN ('', ' ') THEN NULL " +
                            "ELSE LOWER(TRIM(REPLACE(REPLACE(\"" + column + "\", '\n', '_'), ';', ','))) " +
                            "END");
        }
    }

    public static void writeCSV(LinkedList<Map<String, Object>> features, String path, String pathToStoreProfile) {
        try {
            String fileName = Paths.get(path).getFileName().toString();
            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
            String profileFileName = pathToStoreProfile + "\\" + fileNameWithOutExt + "_profile.csv";

            FileWriter csvWriter = new FileWriter(profileFileName);

            // Write header
            Map<String, Object> firstFeature = features.getFirst();
            for (String key : firstFeature.keySet()) {
                csvWriter.append(key);
                csvWriter.append(";");
            }
            csvWriter.append("\n");

            // Write data
            for (Map<String, Object> feature : features) {
                for (Object value : feature.values()) {
                    csvWriter.append(value.toString());
                    csvWriter.append(";");
                }
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<Map<String, Object>> readCSVFile(String path) {
        LinkedList<Map<String, Object>> profile = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            String[] headers = null;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (headers == null) {
                    headers = values;
                } else {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        row.put(headers[i], values[i]);
                    }
                    JSONObject jsonObject = new JSONObject(row);
                    profile.add(jsonObject);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return profile;
    }
}
