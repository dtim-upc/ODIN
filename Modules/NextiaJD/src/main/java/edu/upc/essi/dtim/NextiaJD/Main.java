package edu.upc.essi.dtim.NextiaJD;

import edu.upc.essi.dtim.NextiaJD.predictQuality.PredictQuality;
import edu.upc.essi.dtim.NextiaJD.predictQuality.Profile;
import edu.upc.essi.dtim.NextiaJD.utils.DuckDB;
import org.apache.commons.lang3.tuple.Pair;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import static edu.upc.essi.dtim.NextiaJD.predictQuality.Profile.generateAllProfilesOfAllDataInAFolder;

public class Main {
    public static void main(String[] args)  {
        Connection conn = null;
        try {
            conn = DuckDB.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Profile p = new Profile(conn);
//        // if pathToStoreProfile is left blank (i.e. "") the profile will not be stored in disk
//        // if resultingProfileName is left blank (i.e. "") the profile file name will be the same as the original csv
//        p.createProfile("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\Profiles", "test2");
//        p.createProfile("D:\\Work\\TFM\\Others\\eo_xx.csv", "", "");


//        CalculateQuality cq = new CalculateQuality(conn, 4.0, 1);
//        System.out.println(cq.calculateQualityDiscrete("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\TFM\\Others\\eo_xx.csv", "ein", "ein"));
//        System.out.println(cq.calculateQualityContinuous("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\TFM\\Others\\eo_xx.csv", "ein", "ein"));

//        PredictQuality pq = new PredictQuality(conn);
//        pq.predictQuality("D:\\Projects\\Files\\eo4_profile.json", "D:\\Projects\\Files\\eo_xx_2_profile.json", "NAME", "NAME");

//        pq.calculateDistancesAttVsFolder("Name", "AdventureWorks2014_CountryRegion_profile.csv", "C:\\Work\\NextiaJD\\datasets\\profilesCSV");
//        pq.calculateDistancesForAllProfilesInAFolder("C:\\Work\\NextiaJD\\datasets\\profilesCSV", "C:\\Work\\NextiaJD");

//        pq.predictQuality("C:\\Work\\NextiaJD\\datasets\\profilesCSV\\acquisitions_profile.csv", "C:\\Work\\NextiaJD\\datasets\\profilesCSV\\acquisitions_profile.csv",
//                    "AcquisitionID", "AcquisitionID");


//        santos();
//        tus();
        santosBig();
//        p.createProfile("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_big\\datalake\\gp-prescribing---september-2018.csv",
//                    "C:\\Work");

    }
    public static void santos() {
        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\datalake", "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\profiles");
            Connection conn = DuckDB.getConnection();
            Profile p = new Profile(conn);
            PredictQuality pq = new PredictQuality(conn);

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader  reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\santos_small_benchmark_groundtruth.csv"))) {
                String[] headerLine = reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) {
                    String dataset = line[1];
                    String attribute = line[4]; // the attribute name is the same for the two columns
                    if (!listOfQueryColumns.contains(Pair.of(dataset, attribute))) {
                        listOfQueryColumns.add(Pair.of(dataset, attribute));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Pair<String, String> pair: listOfQueryColumns) {
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\profiles");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void tus() {
        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\tus\\tus_small\\csvfiles", "C:\\Work\\NextiaJD\\other_datasets\\tus\\tus_small\\profiles");
            Connection conn = DuckDB.getConnection();
            Profile p = new Profile(conn);
            PredictQuality pq = new PredictQuality(conn);

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader  reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\tus\\tus_small\\TUS_benchmark_relabeled_groundtruth.csv"))) {
                String[] headerLine = reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) {
                    String dataset = line[1];
                    String attribute = line[4]; // the attribute name is the same for the two columns
                    if (!listOfQueryColumns.contains(Pair.of(dataset, attribute))) {
                        listOfQueryColumns.add(Pair.of(dataset, attribute));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Pair<String, String> pair: listOfQueryColumns) {
                // ELS NOMS DE LES COLUMNES DEL GROUN TRUTH NO COINCIDEIXEN PER A 25 TAULES (LINIA 91)
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\other_datasets\\tus\\tus_small\\profiles");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void santosBig() {
        try {
            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_big\\datalake", "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_big\\profiles");
//            Connection conn = DuckDB.getConnection();
//            Profile p = new Profile(conn);
//            PredictQuality pq = new PredictQuality(conn);
//
//            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
//            try (CSVReader  reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\santos_small_benchmark_groundtruth.csv"))) {
//                String[] headerLine = reader.readNext();
//                String[] line;
//                while ((line = reader.readNext()) != null) {
//                    String dataset = line[1];
//                    String attribute = line[4]; // the attribute name is the same for the two columns
//                    if (!listOfQueryColumns.contains(Pair.of(dataset, attribute))) {
//                        listOfQueryColumns.add(Pair.of(dataset, attribute));
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            for (Pair<String, String> pair: listOfQueryColumns) {
//                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\profiles");
//            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}