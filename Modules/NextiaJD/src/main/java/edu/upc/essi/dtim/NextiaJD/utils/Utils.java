package edu.upc.essi.dtim.NextiaJD.utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
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
        ResultSet rs = stmt.executeQuery("SELECT COLUMN_NAME, TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS");
        while (rs.next()) {
            if (rs.getString(2).equals(tableName)) {
                stmt.execute(
                        "UPDATE \"" + tableName + "\" " +
                                "SET \"" + rs.getString(1) + "\" = CASE " +
                                "WHEN \"" + rs.getString(1) + "\" IN ('', ' ') THEN NULL " +
                                "ELSE LOWER(TRIM(\"" + rs.getString(1) + "\")) " +
                                "END");
            }
        }
    }

    public static void writeJSON(LinkedList<Map<String, Object>> features, String path, String pathToStoreProfile, String resultingProfileName) throws IOException {
        String profileFileName = pathToStoreProfile + "\\" + resultingProfileName + ".json";
        if (resultingProfileName.isEmpty()) {
            String fileName = Paths.get(path).getFileName().toString();
            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
            profileFileName = pathToStoreProfile + "\\" + fileNameWithOutExt + ".json";
        }
        FileWriter file = new FileWriter(profileFileName);
        file.write("[\n");
        int count = 1;
        for (Map<String,Object> map: features) {
            JSONObject json = new JSONObject(map);
            if (count < features.size()) {
                file.write(json + ",\n");
                ++count;
            }
            else file.write(json + "\n");
        }
        file.write("]");
        file.close();
    }
}
