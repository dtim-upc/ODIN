package edu.upc.essi.dtim.NextiaJD;


import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import jakarta.xml.bind.JAXBException;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Discovery implements IDiscovery {
    DataLayer dl;
    public Discovery(DataLayer dl) {
        this.dl = dl;
    }
    @Override
    public double calculateJoinQualityDiscrete(String table1, String table2, String att1, String att2) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        Connection conn = DuckDB.getConnection();
        CalculateQualityNoNextiaCore cq = new CalculateQualityNoNextiaCore(conn, 4.0, 0.5);
        return cq.calculateQualityDiscrete(table1, table2, att1, att2);
    }

    @Override
    public double calculateJoinQualityContinuous(String table1, String table2, String att1, String att2) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        Connection conn = DuckDB.getConnection();
        CalculateQualityNoNextiaCore cq = new CalculateQualityNoNextiaCore(conn, 4.0, 0.5);
        return cq.calculateQualityContinuous(table1, table2, att1, att2);
    }

    @Override
    public JSONArray createProfile(String path, String pathToStoreProfile, String resultingProfileName) throws SQLException, ClassNotFoundException, IOException {
        Connection conn = DuckDB.getConnection();
        Profile p = new Profile(conn);
        return p.createProfile(path, pathToStoreProfile, resultingProfileName);
    }

    @Override
    public double predictJoinQuality(String path1, String path2, String att1, String att2) throws SQLException, ClassNotFoundException, IOException, ParseException, SAXException, JAXBException {
        Connection conn = DuckDB.getConnection();
        PredictQuality pq = new PredictQuality(conn);
        return pq.predictQuality(path1, path2, att1, att2);
    }

    @Override
    public List<Alignment> getAlignments(Dataset d1, Dataset d2) throws Exception {
        List<Alignment> alignments = new ArrayList<>();

        for (Attribute a1: d1.getAttributes()) {
            for (Attribute a2: d2.getAttributes()) {
                double containment = 0.0;
                double cardinality1 = 0.0;
                double cardinality2 = 0.0;
                ResultSet rs = dl.executeQuery("SELECT COUNT(DISTINCT " + d1.getUUID() + "." + a1.getName() + ") " +
                        "FROM " + d1.getUUID() + " " +
                        "WHERE " + d1.getUUID() + "." + a1.getName() + " IN (SELECT DISTINCT " + d2.getUUID() + "." + a2.getName() + " " +
                        "FROM " + d2.getUUID() + ")", new Dataset[]{d1, d2});
                while (rs.next()) {
                    containment = rs.getDouble(1);
                }

                rs = dl.executeQuery("SELECT COUNT(DISTINCT " + a1.getName() + ") FROM " + d1.getUUID(), new Dataset[]{d1, d2});
                while (rs.next()) {
                    cardinality1 = rs.getDouble(1);
                }

                rs = dl.executeQuery("SELECT COUNT(DISTINCT " + a2.getName() + ") FROM " + d2.getUUID(), new Dataset[]{d1, d2});
                while (rs.next()) {
                    cardinality2 = rs.getDouble(1);
                }

                double cardinality_proportion = Math.min(cardinality1, cardinality2)/Math.max(cardinality1, cardinality2);
                CalculateQuality cq = new CalculateQuality(1.0,1);
                double quality = cq.calculateQualityContinuous(containment, cardinality_proportion);
                Alignment a = new Alignment(a1, a2, "", (float) quality);
                alignments.add(a);
                rs.close();
            }
        }
        return alignments;
    }
}
