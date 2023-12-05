package edu.upc.essi.dtim.NextiaJD;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static edu.upc.essi.dtim.NextiaJD.Utils.getNumberOfValues;
import static edu.upc.essi.dtim.NextiaJD.Utils.preprocessing;

public class CalculateQualityNoNextiaCore {

    Connection conn;
    String table1 = "temptTable1";
    String table2 = "temptTable2";
    Double l; // Levels to the discrete quality
    Double s; // Strictness of the continuous quality

    public CalculateQualityNoNextiaCore(Connection conn, Double l, double s) {this.conn = conn; this.l = l; this.s = s;}

    public double calculateQualityDiscrete(String path1, String path2, String att1, String att2) throws SQLException{
        Double[] parameters = getParameters(path1, path2, att1, att2);
        Double c = parameters[0];
        Double k = parameters[1];
        for (double i = 0; i<l; ++i) {
            if ((c >= 1-(i/l)) && (k >= Math.pow(0.5, i))) {
                return (l-i+1)/l;
            }
        }
        return 0.0;
    }

    public double calculateQualityContinuous(String path1, String path2, String att1, String att2) throws SQLException{
        Double[] parameters = getParameters(path1, path2, att1, att2);
        Double c = parameters[0];
        Double k = parameters[1];

        // The native Java implementation does not work, so as of now we use a Python subroutine
        Double[] mu_v = {0.44 + 0.25 * s, 0 + 0.25 * s}; // Mean vector in terms of strictness
        Double[] sigma_v = {0.19, 0.28}; // Variance vector (we assume an equal sigma for C and K)
        // Defining C limits
        double lowerBoundC = (-mu_v[0]) / sigma_v[0];
        double upperBoundC = (1 - mu_v[0]) / sigma_v[0];
        // Defining K limits
        double lowerBoundK = (-mu_v[1]) / sigma_v[1];
        double upperBoundK = (1 - mu_v[1]) / sigma_v[1];

        // In brackets the version that obtains the same results as the Python code
        //TND tnd_c = new TND(mu_v[0], sigma_v[0], lowerBoundC * sigma_v[0] + mu_v[0], upperBoundC * sigma_v[0] + mu_v[0]);
        TruncatedNormalDistribution tnd_c = new TruncatedNormalDistribution(mu_v[0], sigma_v[0], lowerBoundC, upperBoundC);

        //TND tnd_k = new TND(mu_v[1], sigma_v[1], lowerBoundK * sigma_v[1] + mu_v[1], upperBoundK * sigma_v[1] + mu_v[1]);
        TruncatedNormalDistribution tnd_k = new TruncatedNormalDistribution(mu_v[1], sigma_v[1], lowerBoundK, upperBoundK);

        double cdf_c = tnd_c.cdf(c);
        double cdf_k = tnd_k.cdf(k);
        return cdf_c * cdf_k;
    }

    private Double[] getParameters(String path1, String path2, String att1, String att2) throws SQLException{
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE " + table1 + " AS SELECT " + att1 + " FROM read_csv_auto('" + path1 + "', header=True, all_varchar=True)");
        stmt.execute("CREATE TABLE " + table2 + " AS SELECT " + att2 + " FROM read_csv_auto('" + path2 + "', header=True, all_varchar=True)");
        preprocessing(conn, table1);
        preprocessing(conn, table2);

        Double c = getContainment(att1, att2);
        Double k = getCardinalityProportion(att1, att2);

        stmt.execute("DROP TABLE \"" + table1 + "\"");
        stmt.execute("DROP TABLE \"" + table2 + "\"");

        return new Double[]{c,k};
    }

    // Same as calculateQuality but calculates the qualities of the cartesian product of the attributes of the
    // two dataset. It generates a map with all the qualities that, as of now, is only printed through the console.
    public void calculateQualityForDatasets(String path1, String path2) throws SQLException, IOException, InterruptedException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE " + table1 + " AS SELECT * FROM read_csv_auto('" + path1 + "')");
        stmt.execute("CREATE TABLE " + table2 + " AS SELECT * FROM read_csv_auto('" + path2 + "')");
        preprocessing(conn, table1);
        preprocessing(conn, table2);

        LinkedList<Map<String,Object>> qualities = new LinkedList<>();

        ResultSet rs = stmt.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table1 + "'");
        while (rs.next()) {
            String att1 = rs.getString(1);
            ResultSet rs2 = stmt.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table2 + "'");
            while (rs2.next()) {
                String att2 = rs2.getString(1);
                Map<String,Object> quality = new HashMap<>();

                quality.put("ds_name", Paths.get(path1).getFileName().toString());
                quality.put("att_name", att1);
                quality.put("ds2_name", Paths.get(path2).getFileName().toString());
                quality.put("att2_name", att2);

                Double c = getContainment(att1, att2);
                quality.put("c", c);
                Double k = getCardinalityProportion(att1, att2);
                quality.put("k", k);

                quality.put("discrete_quality", calculateQualityDiscrete(path1,att1,path2,att2));

                qualities.add(quality);
            }
        }
        stmt.execute("DROP TABLE \"" + table1 + "\"");
        stmt.execute("DROP TABLE \"" + table2 + "\"");
        //System.out.println(qualities);
    }


    private Double getContainment(String att1, String att2) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT d1." + att1 + ") " +
                "FROM " + table1 + " d1 " + "" +
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
