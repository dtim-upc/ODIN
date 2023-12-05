package edu.upc.essi.dtim.NextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.ApiRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import edu.upc.essi.dtim.NextiaDataLayer.implementations.DLDuckDB;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception {

        CsvDataset d1 = new CsvDataset("d1", "d1", "description1", "src\\main\\resources\\sample.csv");
        CsvDataset d2 = new CsvDataset("titanic", "titanic", "description2", "C:\\Work\\Files\\titanic.csv");
        JsonDataset d3 = new JsonDataset("cats", "cats", "description3", "C:\\Work\\Files\\cats_simplified.json");
        CsvDataset d4 = new CsvDataset("airtravel", "airtravel", "description3", "C:\\Work\\Files\\airtravel.csv");

        CsvDataset d5 = new CsvDataset("titanic1", "titanic1", "description2", "C:\\Work\\Files\\titanic1.csv");
        CsvDataset d6 = new CsvDataset("titanic2", "titanic2", "description2", "C:\\Work\\Files\\titanic2.csv");

        SQLDataset d7 = new SQLDataset("sql", "personas", "", "personas", "dtim.essi.upc.edu", "5432", "vasenjo", "jBGRfEu");
        APIDataset d8 = new APIDataset("api", "fact", "", "fact", "");



//        testMultipleAccess(d5,d6);

        testSQL(d7);

//        testAPI(d8);

//      Virtualized access tests. In duckDB it does not close correctly, whereas Spark can not execute due to two SparkContext at the same time
        //d4.setWrapper("SELECT Month AS Month, A1958 AS  A1958, A1959 AS  A1959, A1960 AS  A1960 FROM airtravel\n");
        //DLDuckDB dl = new DLDuckDB("C:\\Work\\database");
        //dl.temporalAccess(d4, "d4");
        //ResultSet rs = dl.executeQueryTemporalAccess("SELECT * FROM d4");
        //while (rs.next()) {
            // System.out.println(rs.getDouble(2));
            //}
        //rs.close();
        //dl.close();


//        d3.setUUID("cats");
//        d3.setWrapper("SELECT fact,length FROM cats\n");
//        DataLoading dl = new DataLoading("C:\\Work\\Database");
//        dl.uploadToLandingZone(d3);
//        dl.close();
//
//        DataLayer dlm = new DLDuckDB("C:\\Work\\Database");
//        dlm.uploadToFormattedZone(d3, "formatted" + d3.setUUID());
//
//        dlm.virtualizeTable("formatted" + d3.setUUID(), "d1_temp",  "formattedZone");
//
//        ResultSet rs = dlm.executeQuery("SELECT * FROM d1_temp");
//        while (rs.next()) {
//            System.out.println(rs.getString(1));
//        }
////        // Spark no permet fer updates
////        dlm.execute("UPDATE d1_temp SET col0 = 'name'");
////        rs = dlm.executeQuery("SELECT * FROM d1_temp");
////        while (rs.next()) {
////            System.out.println(rs.getString(2));
////        }
//        dlm.RemoveFromFormattedZone("formatted" + d2.getUUID());
//        dlm.close();
    }

    private static void testSQL(SQLDataset d7) throws SQLException, IOException, ClassNotFoundException {
        RelationalJDBCRepository dr = new RelationalJDBCRepository();
        dr.setUrl("jdbc:postgresql://dtim.essi.upc.edu:5432/odin_test");
        dr.setVirtual(false);
        d7.setRepository(dr);
        d7.setUUID("personas");
        d7.setWrapper("SELECT * FROM personas\n");

        DLDuckDB dlm = new DLDuckDB("C:\\Work\\Database");
        DataLoading dl = dlm.getDataLoading();
        dl.uploadToLandingZone(d7);
        dl.close();
        dlm.uploadToFormattedZone(d7, d7.getUUID());

        Dataset[] datasets = new Dataset[]{d7};
        ResultSet rs = dlm.executeQuery("SELECT * FROM personas", datasets);
        while(rs.next()) {
            System.out.println(rs.getString(1));
        }
//        dlm.close();
    }

    private static void testAPI(APIDataset d8) throws SQLException, IOException, ClassNotFoundException {
        ApiRepository dr = new ApiRepository();
        dr.setUrl("https://catfact.ninja/");
        dr.setVirtual(true);
        d8.setRepository(dr);
        d8.setUUID("fact");
        d8.setWrapper("SELECT fact,length FROM fact");

        DLDuckDB dlm = new DLDuckDB("C:\\Work\\Database");
//        DataLoading dl = dlm.getDataLoading();
//        dl.uploadToLandingZone(d8);
//        dl.close();
//        dlm.uploadToFormattedZone(d8, d8.getUUID());

        Dataset[] datasets = new Dataset[]{d8};
        ResultSet rs = dlm.executeQuery("SELECT * FROM fact", datasets);
        while(rs.next()) {
            System.out.println(rs.getString(1));
        }
        dlm.close();
    }

    public static void testMultipleAccess(Dataset d1, Dataset d2) throws SQLException, IOException, ClassNotFoundException {
        // d1 is materialized, d2 is virtualized
        // All of this is done in ODIN when creating the datasets (graph and data for d1, graph for d2)
        DataRepository dr1 = new LocalRepository();
        dr1.setVirtual(false);
        d1.setRepository(dr1);
        DataRepository dr2 = new ApiRepository();
        dr2.setVirtual(true);
        d2.setRepository(dr2);
        d1.setUUID("titanic1");
        d2.setUUID("titanic2");
        d1.setWrapper("SELECT * FROM titanic1\n");
        d2.setWrapper("SELECT * FROM titanic2\n");
        DataLoading dl = new DataLoading("C:\\Work\\Database");
        dl.uploadToLandingZone(d1);
        dl.close();
        DLDuckDB dlm = new DLDuckDB("C:\\Work\\Database");
        dlm.uploadToFormattedZone(d1, d1.getUUID());
        /////////////////////////////////////////////////////////////////////////
        Dataset[] datasets = new Dataset[]{d1, d2};
        ResultSet rs = dlm.executeQuery("SELECT * FROM titanic1", datasets);
        while(rs.next()) {
            System.out.println(rs.getString(1));
        }
        dlm.close();

    }
}