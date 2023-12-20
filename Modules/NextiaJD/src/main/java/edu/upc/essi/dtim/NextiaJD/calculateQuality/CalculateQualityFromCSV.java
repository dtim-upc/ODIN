package edu.upc.essi.dtim.NextiaJD.calculateQuality;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static edu.upc.essi.dtim.NextiaJD.utils.Utils.getNumberOfValues;
import static edu.upc.essi.dtim.NextiaJD.utils.Utils.preprocessing;

public class CalculateQualityFromCSV {

    Connection conn;
    String table1 = "temptTable1";
    String table2 = "temptTable2";
    Double l; // Levels to the discrete quality
    Double s; // Strictness of the continuous quality
    CalculateQuality cq;

    public CalculateQualityFromCSV(Connection conn, Double l, double s) {
        this.conn = conn;
        this.l = l;
        this.s = s;
        this.cq = new CalculateQuality(l,s);
    }

    public double calculateQualityDiscreteFromCSV(String CSVPath1, String CSVPath2, String att1, String att2) throws SQLException{
        Double[] parameters = getParameters(CSVPath1, CSVPath2, att1, att2);
        Double c = parameters[0];
        Double k = parameters[1];

        return cq.calculateQualityDiscrete(c,k);
    }

    public double calculateQualityContinuousFromCSV(String path1, String path2, String att1, String att2) throws SQLException{
        Double[] parameters = getParameters(path1, path2, att1, att2);
        Double c = parameters[0];
        Double k = parameters[1];

        return cq.calculateQualityContinuous(c,k);
    }

    // Same as calculateQuality but calculates the qualities of the cartesian product of the attributes of the
    // two dataset. It generates a map with all the qualities that, as of now, is only printed through the console.
    public LinkedList<Map<String, Object>> calculateQualityForDatasets(String CSVPath1, String CSVPath2) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE " + table1 + " AS SELECT * FROM read_csv_auto('" + CSVPath1 + "')");
        stmt.execute("CREATE TABLE " + table2 + " AS SELECT * FROM read_csv_auto('" + CSVPath2 + "')");
        preprocessing(conn, table1);
        preprocessing(conn, table2);

        LinkedList<Map<String,Object>> qualities = new LinkedList<>();

        ResultSet rs = stmt.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table1 + "'");
        while (rs.next()) {
            String att1 = rs.getString(1);
            ResultSet rs2 = stmt.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table2 + "'");
            while (rs2.next()) {
                String att2 = rs2.getString(1);
                Map<String, Object> quality = new HashMap<>();

                quality.put("ds_name", Paths.get(CSVPath1).getFileName().toString());
                quality.put("att_name", att1);
                quality.put("ds2_name", Paths.get(CSVPath2).getFileName().toString());
                quality.put("att2_name", att2);

                Double c = getContainment(att1, att2);
                quality.put("c", c);
                Double k = getCardinalityProportion(att1, att2);
                quality.put("k", k);

                quality.put("discrete_quality", calculateQualityDiscreteFromCSV(CSVPath1,att1,CSVPath2,att2));
                quality.put("continuous_quality", calculateQualityContinuousFromCSV(CSVPath1,att1,CSVPath2,att2));

                qualities.add(quality);
            }
        }
        stmt.execute("DROP TABLE \"" + table1 + "\"");
        stmt.execute("DROP TABLE \"" + table2 + "\"");
        return qualities;
    }

    private Double[] getParameters(String CSVPath1, String CSVPath2, String att1, String att2) throws SQLException{
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE " + table1 + " AS SELECT " + att1 + " FROM read_csv_auto('" + CSVPath1 + "', header=True, all_varchar=True)");
        stmt.execute("CREATE TABLE " + table2 + " AS SELECT " + att2 + " FROM read_csv_auto('" + CSVPath2 + "', header=True, all_varchar=True)");
        preprocessing(conn, table1);
        preprocessing(conn, table2);

        Double c = getContainment(att1, att2);
        Double k = getCardinalityProportion(att1, att2);

        stmt.execute("DROP TABLE \"" + table1 + "\"");
        stmt.execute("DROP TABLE \"" + table2 + "\"");

        return new Double[]{c,k};
    }

    private Double getContainment(String att1, String att2) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT d1." + att1 + ") " +
                "FROM " + table1 + " d1 " +
                "WHERE d1." + att1 + " IN (SELECT DISTINCT d2." + att2 + " " +
                "FROM " + table2 + " d2)");
        rs.next();
        return rs.getInt(1)/ getNumberOfValues(conn, table1, att1);
    }

    private Double getCardinalityProportion(String att1, String att2) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT \"" + att1 + "\") FROM " + table1);
        rs.next();
        double cardinality1 = rs.getDouble(1);

        rs = stmt.executeQuery("SELECT COUNT(DISTINCT \"" + att2 + "\") FROM " + table2);
        rs.next();
        double cardinality2 = rs.getDouble(1);

        return Math.min(cardinality1, cardinality2)/Math.max(cardinality1, cardinality2);
    }

}
