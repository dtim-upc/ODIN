package edu.upc.essi.dtim.NextiaJD.discovery;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.NextiaJD.utils.DuckDB;
import edu.upc.essi.dtim.NextiaJD.calculateQuality.CalculateQuality;
import edu.upc.essi.dtim.NextiaJD.calculateQuality.CalculateQualityFromCSV;
import edu.upc.essi.dtim.NextiaJD.predictQuality.PredictQuality;
import edu.upc.essi.dtim.NextiaJD.predictQuality.Profile;
import jakarta.xml.bind.JAXBException;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Discovery implements IDiscovery {
    DataLayer dl;
    public Discovery(DataLayer dl) {
        this.dl = dl;
    }
    @Override
    public double calculateJoinQualityDiscreteFromCSV(String CSVPath1, String CSVPath2, String att1, String att2) {
        try {
            Connection conn = DuckDB.getConnection();
            CalculateQualityFromCSV cq = new CalculateQualityFromCSV(conn, 4.0, 0.5);
            return cq.calculateQualityDiscreteFromCSV(CSVPath1, CSVPath2, att1, att2);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double calculateJoinQualityContinuousFromCSV(String CSVPath1, String CSVPath2, String att1, String att2) {
        try {
            Connection conn = DuckDB.getConnection();
            CalculateQualityFromCSV cq = new CalculateQualityFromCSV(conn, 4.0, 0.5);
            return cq.calculateQualityContinuousFromCSV(CSVPath1, CSVPath2, att1, att2);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LinkedList<Map<String, Object>> calculateQualitiesFromDatasets(String CSVPath1, String CSVPath2, String qualityType) {
        if (qualityType.equals("continuous") || qualityType.equals("discrete")) {
            throw new RuntimeException("Type of quality needs to be 'continuous' or 'discrete'");
        }
        else {
            try {
                Connection conn = DuckDB.getConnection();
                CalculateQualityFromCSV cq = new CalculateQualityFromCSV(conn, 4.0, 0.5);
                return cq.calculateQualityForDatasets(CSVPath1, CSVPath2);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public JSONArray createProfile(String path, String pathToStoreProfile, String resultingProfileName) throws SQLException, IOException {
        Connection conn = DuckDB.getConnection();
        Profile p = new Profile(conn);
        return p.createProfile(path, pathToStoreProfile, resultingProfileName);
    }

    @Override
    public double predictJoinQuality(String path1, String path2, String att1, String att2) throws SQLException, IOException, ParseException, SAXException, JAXBException {
        Connection conn = DuckDB.getConnection();
        PredictQuality pq = new PredictQuality(conn);
        return pq.predictQuality(path1, path2, att1, att2);
    }

    @Override
    public List<Alignment> getAlignments(Dataset d1, Dataset d2) {
        List<Alignment> alignments = new ArrayList<>();
        String d1Name = "for_" + d1.getUUID();
        String d2Name = "for_" + d2.getUUID();

        for (Attribute a1: d1.getAttributes()) {
            for (Attribute a2: d2.getAttributes()) {
                try {
                    double containment = 0.0;
                    double cardinality1 = 0.0;
                    double cardinality2 = 0.0;
                    ResultSet rs = dl.executeQuery("SELECT COUNT(DISTINCT " + d1Name + "." + a1.getName() + ") " +
                            "FROM " + d1Name + " " +
                            "WHERE " + d1Name + "." + a1.getName() + " IN (SELECT DISTINCT " + d2Name + "." + a2.getName() + " " +
                            "FROM " + d2Name + ")", new Dataset[]{d1, d2});
                    while (rs.next()) {
                        containment = rs.getDouble(1);
                    }

                    rs = dl.executeQuery("SELECT COUNT(DISTINCT " + a1.getName() + ") " +
                            "FROM " + d1Name, new Dataset[]{d1, d2});
                    while (rs.next()) {
                        cardinality1 = rs.getDouble(1);
                    }

                    rs = dl.executeQuery("SELECT COUNT(DISTINCT " + a2.getName() + ") " +
                            "FROM " + d2Name, new Dataset[]{d1, d2});
                    while (rs.next()) {
                        cardinality2 = rs.getDouble(1);
                    }

                    double cardinality_proportion = Math.min(cardinality1, cardinality2) / Math.max(cardinality1, cardinality2);
                    CalculateQuality cq = new CalculateQuality(1.0, 1);
                    double quality = cq.calculateQualityContinuous(containment, cardinality_proportion);
                    Alignment a = new Alignment(a1, a2, "", (float) quality);
                    alignments.add(a);
                    rs.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return alignments;
    }
}
