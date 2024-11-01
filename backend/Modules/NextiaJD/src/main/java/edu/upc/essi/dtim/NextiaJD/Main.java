package edu.upc.essi.dtim.NextiaJD;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import edu.upc.essi.dtim.NextiaJD.predictQuality.PredictQuality;
import edu.upc.essi.dtim.NextiaJD.predictQuality.Profile;
import edu.upc.essi.dtim.NextiaJD.utils.DuckDB;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Main {
    public static void main(String[] args)  {
        if (args.length < 1) {
            System.out.println("No function specified.");
            return;
        }

        String functionName = args[0];

        switch (functionName) {
            case "createProfile":
                if (args.length != 3) {
                    System.out.println("2 parameters required: (1) the dataset (CSV) path and (2) the path to store the profile");
                    break;
                }
                System.out.println("Computing the profile");
                createProfile(args[1], args[2]);
                break;
            case "computeDistances":
                if (args.length != 5) {
                    System.out.println("4 parameters required: (1) the name of the dataset that contains the query column, (2) the query column name");
                    System.out.println("(3) the path to the list of profiles to compute distances with and (4) the path to store the resulting distances");
                    break;
                }
                System.out.println("Computing the distances");
                computeDistances(args[1], args[2], args[3], args[4]);
                break;
            case "computeDistancesForBenchmark":
                if (args.length != 4 && args.length != 6) {
                    System.out.println("3 or 5 parameters required");
                    System.out.println("The first three have to be (1) path to the ground truth, (2) path to the profiles and (3) path to store the distances");
                    System.out.println("If the ground truth has the columns target_ds and target_attr, then it is enough.");
                    System.out.println("Otherwise, the names of the columns corresponding to (4) the query dataset and (5) query attribute of the ground truth need to be added");
                    break;
                }
                System.out.println("Computing the distances");
                if (args.length == 4) {
                    calculateDistancesForBenchmark(args[1], args[2], args[3], "target_ds", "target_attr");
                }
                else {
                    calculateDistancesForBenchmark(args[1], args[2], args[3], args[4], args[5]);
                }
                break;
            default:
                System.out.println("Unknown function: " + functionName);
                break;
        }
    }

    public static void createProfile(String filePath, String profilePath) {
        Connection conn;
        try {
            conn = DuckDB.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Profile p = new Profile(conn);

        p.createProfile(filePath, profilePath);
    }

    public static void computeDistances(String dataset, String attribute, String profilesPath, String pathToStore) {
        PredictQuality pq = new PredictQuality();
        pq.calculateDistancesAttVsFolder(dataset, attribute, profilesPath, pathToStore, false);
    }

    public static void calculateDistancesForBenchmark(String groundTruthPath, String profilesPath, String distancesPath,
                                                      String queryDatasetColumnName, String queryAttributeColumnName) {
        try {
            PredictQuality pq = new PredictQuality();

            List<Pair<String, String>> listOfQueryColumns = new ArrayList<>();
            Set<Pair<String, String>> seenPairs = new HashSet<>();

            // From the ground truth we extract for which columns we need to compute the distances
            try (CSVParser parser = new CSVParser(new FileReader(Paths.get(groundTruthPath).toFile()),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                for (CSVRecord record : parser) {
                    String dataset = record.get(queryDatasetColumnName);
                    String attribute = record.get(queryAttributeColumnName);
                    Pair<String, String> pair = Pair.of(dataset, attribute);

                    if (seenPairs.add(pair)) { // Only add if it's not already in the set
                        listOfQueryColumns.add(pair);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading the CSV file: " + e.getMessage());
            }

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < listOfQueryColumns.size(); i++) {
                Pair<String, String> pair = listOfQueryColumns.get(i);
                String dataset = pair.getLeft();
                String attribute = pair.getRight();
                pq.calculateDistancesAttVsFolder(dataset, attribute, profilesPath, distancesPath, false);

                System.out.printf("Query column %d out of %d%n", i + 1, listOfQueryColumns.size());
            }

            long endTime = System.currentTimeMillis();
            System.out.printf("Execution time: %.2f seconds%n", (endTime - startTime) / 1000.0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}