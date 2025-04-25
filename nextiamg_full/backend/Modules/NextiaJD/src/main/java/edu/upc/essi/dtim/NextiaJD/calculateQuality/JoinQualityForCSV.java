package edu.upc.essi.dtim.NextiaJD.calculateQuality;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static edu.upc.essi.dtim.NextiaJD.utils.Utils.preprocessing;

public class JoinQualityForCSV {
    Connection conn;
    String table1 = "tempTable1";
    String table2 = "tempTable2";
    double l; // Levels to the discrete quality
    double s; // Strictness of the continuous quality
    JoinQuality jq;

    public JoinQualityForCSV(Connection conn, double l, double s) {
        this.conn = conn;
        this.l = l;
        this.s = s;
        this.jq = new JoinQuality(l,s);
    }

    public double joinQualityForCSV(String CSVPath1, String CSVPath2, String att1, String att2, String qualityType) throws SQLException{
        double[] parameters = getParameters(CSVPath1, CSVPath2, att1, att2);
        double mj = parameters[0];
        double k = parameters[1];

        if (qualityType.equals("discrete")) {
            return jq.discreteQuality(mj,k);
        }
        else {
            return jq.continuousQuality(mj,k);
        }
    }

    // Same as calculateQuality but calculates the qualities of the cartesian product of the attributes of the
    // two datasets. It generates a map with all the qualities that, as of now, is only printed through the console.
    public LinkedList<Map<String, Object>> calculateQualityForDatasets(String CSVPath1, String CSVPath2) throws SQLException {
        Statement stmt = conn.createStatement();
        // Create DuckDB tables for the CSV files and preprocess them
        stmt.execute("CREATE TABLE " + table1 + " AS SELECT * FROM read_csv_auto('" + CSVPath1 + "')");
        stmt.execute("CREATE TABLE " + table2 + " AS SELECT * FROM read_csv_auto('" + CSVPath2 + "')");
        preprocessing(conn, table1);
        preprocessing(conn, table2);

        LinkedList<Map<String,Object>> qualities = new LinkedList<>();

        // For every column of CSV 1, obtain join quality with all columns of CSV 2
        ResultSet rs = stmt.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table1 + "'");
        while (rs.next()) {
            String att1 = rs.getString(1);
            ResultSet rs2 = stmt.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table2 + "'");
            while (rs2.next()) {
                String att2 = rs2.getString(1);
                Map<String, Object> quality = new HashMap<>();

                quality.put("ds_name", Paths.get(CSVPath1).getFileName().toString());
                quality.put("att_name", att1);
                quality.put("ds_name_2", Paths.get(CSVPath2).getFileName().toString());
                quality.put("att_name_2", att2);

                double mj = getMultisetJaccard(att1, att2);
                quality.put("mj", getMultisetJaccard(att1, att2));
                double k = getCardinalityProportion(att1, att2);
                quality.put("k", k);

                quality.put("discrete_quality", jq.discreteQuality(mj, k));
                quality.put("continuous_quality", jq.continuousQuality(mj, k));

                qualities.add(quality);
            }
        }
        stmt.execute("DROP TABLE \"" + table1 + "\"");
        stmt.execute("DROP TABLE \"" + table2 + "\"");
        return qualities;
    }

    private double[] getParameters(String CSVPath1, String CSVPath2, String att1, String att2) throws SQLException{
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE " + table1 + " AS SELECT " + att1 + " FROM read_csv_auto('" + CSVPath1 + "', header=True, all_varchar=True)");
        stmt.execute("CREATE TABLE " + table2 + " AS SELECT " + att2 + " FROM read_csv_auto('" + CSVPath2 + "', header=True, all_varchar=True)");
        preprocessing(conn, table1);
        preprocessing(conn, table2);

        double mj = getMultisetJaccard(att1, att2);
        double k = getCardinalityProportion(att1, att2);

        stmt.execute("DROP TABLE \"" + table1 + "\"");
        stmt.execute("DROP TABLE \"" + table2 + "\"");

        return new double[]{mj,k};
    }

    private double getCardinalityProportion(String att1, String att2) throws SQLException {
        double cardinality1 = getCountDistinct(table1, att1);
        double cardinality2 = getCountDistinct(table2, att2);

        return Math.min(cardinality1, cardinality2)/Math.max(cardinality1, cardinality2);
    }

    private double getMultisetJaccard(String att1, String att2) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT \"" + att1 + "\") " +
                "FROM  \"" + table1 + "\" t1" +
                "INNER JOIN  \"" + table2 + "\" t2" +
                "ON t1.\"" + att1 + "\" = t2.\"" + att2 + "\"");
        rs.next();
        double intersection = rs.getDouble(1);

        double countTable1 = getCount(table1, att1);
        double countTable2 = getCount(table2, att2);

        return intersection / (countTable1 + countTable2);
    }

    private double getCount(String table, String att1) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(\"" + att1 + "\") FROM \"" + table + "\"");
        rs.next();
        return rs.getDouble(1);
    }

    private double getCountDistinct(String table, String att1) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT \"" + att1 + "\") FROM \"" + table + "\"");
        rs.next();
        return rs.getDouble(1);
    }

}
