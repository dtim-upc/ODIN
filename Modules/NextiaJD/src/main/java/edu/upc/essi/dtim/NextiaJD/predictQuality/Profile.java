package edu.upc.essi.dtim.NextiaJD.predictQuality;

import edu.upc.essi.dtim.NextiaJD.utils.DuckDB;
import org.json.simple.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.charset.*;
import java.io.IOException;
import java.util.List;

import static edu.upc.essi.dtim.NextiaJD.predictQuality.FeatureGeneration.*;
import static edu.upc.essi.dtim.NextiaJD.utils.Utils.*;

public class Profile {
    Connection conn;
    String tableName = "temptTable";

    public Profile(Connection conn) {
        this.conn = conn;
    }

    public Profile(Connection conn, String tableName) {
        this.conn = conn;
        this.tableName = tableName;
    }

    public JSONArray createProfile(String dataPath, String pathToStoreProfile) {
        try {
            // Create table from file, handle some errors and preprocess the data (trim and lowercase)
            Statement stmt = conn.createStatement();
            try {
                stmt.execute("CREATE TABLE \"" + tableName + "\" AS SELECT * FROM read_csv_auto('" + dataPath + "', header=True, sample_size=100, ignore_errors=true)");
            } catch (SQLException e) {
                if (e.toString().contains("Invalid unicode (byte sequence mismatch) detected in CSV file")) {
                    fixUnicodeIssue(dataPath);
//                    stmt = conn.createStatement();
                    stmt.execute("CREATE TABLE \"" + tableName + "\" AS SELECT * FROM read_csv_auto('" + dataPath + "', header=True, sample_size=100, ignore_errors=true)");
                } else if (e.toString().contains("CSV File not supported for multithreading")) {
//                    stmt = conn.createStatement();
                    stmt.execute("CREATE TABLE \"" + tableName + "\" AS SELECT * FROM read_csv_auto('" + dataPath + "', header=True, sample_size=100, ignore_errors=True, parallel=false)");
                } else {
                    throw new RuntimeException(e);
                }
            }

            preprocessing(conn, tableName);

            // Generate the profile of the table: for each column, its profile is generated and added to the features variable
            LinkedList<Map<String,Object>> features = new LinkedList<>();
            ResultSet rs = stmt.executeQuery("DESCRIBE \"" + tableName + "\"");
            while (rs.next()) {
                ResultSet rs2 = conn.createStatement().executeQuery("SELECT \"" + rs.getString(1) + "\" FROM \"" + tableName + "\" LIMIT 0");
                ResultSetMetaData rsmd = rs2.getMetaData();
                // We only generate the profile if the column is VARCHAR and has some value (i.e. if it is only null values, we do not create the profile)
                if (rsmd.getColumnTypeName(1).equals("VARCHAR") && getNumberOfValues(conn, tableName, rs.getString(1)) != 0.0) {
                    features.add(createProfileOfColumn(rs.getString(1)));
                }
            }

            // Add name of the dataset to each column (that is, the name of the CSV file)
            for (Map<String,Object> map: features) {
                map.put("dataset_name", Paths.get(dataPath).getFileName().toString());
            }

            // Write the profile in a CSV file (if a path is indicated)
            if (!pathToStoreProfile.isEmpty() && !features.isEmpty()) {
                writeCSV(features, dataPath, pathToStoreProfile);
            }

            // Return the JSON profile to the process that invokes the function
            JSONArray json = new JSONArray();
            json.addAll(features);
            stmt.execute("DROP TABLE \"" + tableName + "\"");
            stmt.close();
            conn.close();

            return json;

        } catch (SQLException e) {
            // We have to remove the table if it has been created. Otherwise, the remaining profiles will not be generated
            try {
                Statement stmt = conn.createStatement();
                stmt.execute("DROP TABLE IF EXISTS \"" + tableName + "\"");
            }  catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    public Map<String,Object> createProfileOfColumn(String column) throws SQLException {
        Map<String, Object> columnFeatures = new HashMap<>();
        addCardinalityFeatures(column, columnFeatures);
        addValueDistributionFeatures(column, columnFeatures);
        addSyntacticFeatures(column, columnFeatures);
        addOtherFeatures(column, columnFeatures);
        columnFeatures.put("attribute_name", "\"" + column + "\""); // Add name of the column
        return columnFeatures;
    }

    public void addCardinalityFeatures(String column, Map<String, Object> columnFeatures) throws SQLException {
        Map<String, Object> newFeatures = generateCardinalityFeatures(conn, tableName, column);
        columnFeatures.put("cardinality", newFeatures.get("cardinality"));
        columnFeatures.put("uniqueness", newFeatures.get("uniqueness"));
        columnFeatures.put("incompleteness", newFeatures.get("incompleteness"));

        newFeatures = generateEntropy(conn, tableName, column);
        columnFeatures.put("entropy", newFeatures.get("entropy"));
    }

    public void addValueDistributionFeatures(String column, Map<String, Object> columnFeatures) throws SQLException {
        Map<String, Object> newFeatures = generateFrequenciesAndPercentages(conn, tableName, column);
        columnFeatures.put("frequency_avg", newFeatures.get("frequency_avg"));
        columnFeatures.put("frequency_min", newFeatures.get("frequency_min"));
        columnFeatures.put("frequency_max", newFeatures.get("frequency_max"));
        columnFeatures.put("frequency_sd", newFeatures.get("frequency_sd"));
        columnFeatures.put("frequency_iqr", newFeatures.get("frequency_iqr"));
        columnFeatures.put("val_pct_min", newFeatures.get("val_pct_min"));
        columnFeatures.put("val_pct_max", newFeatures.get("val_pct_max"));
        columnFeatures.put("val_pct_std", newFeatures.get("val_pct_std"));
        columnFeatures.put("constancy", newFeatures.get("constancy"));

        newFeatures = generateFrequentWordContainment(conn, tableName, column);
        columnFeatures.put("freq_word_containment", newFeatures.get("freq_word_containment"));
        columnFeatures.put("freq_word_soundex_containment", newFeatures.get("freq_word_soundex_containment"));

        newFeatures = generateOctiles(conn, tableName, column);
        columnFeatures.put("frequency_1qo", newFeatures.get("frequency_1qo"));
        columnFeatures.put("frequency_2qo", newFeatures.get("frequency_2qo"));
        columnFeatures.put("frequency_3qo", newFeatures.get("frequency_3qo"));
        columnFeatures.put("frequency_4qo", newFeatures.get("frequency_4qo"));
        columnFeatures.put("frequency_5qo", newFeatures.get("frequency_5qo"));
        columnFeatures.put("frequency_6qo", newFeatures.get("frequency_6qo"));
        columnFeatures.put("frequency_7qo", newFeatures.get("frequency_7qo"));
    }

    public void addSyntacticFeatures(String column, Map<String, Object> columnFeatures) throws SQLException {
//        Map<String, Object> newFeatures = generateDatatypes(conn, tableName, column);
//        columnFeatures.put("datatype", newFeatures.get("datatype"));
//        columnFeatures.put("specific_type", newFeatures.get("specific_type"));
//
//        String[] datatypeLabels = {"pct_numeric", "pct_alphanumeric", "pct_alphabetic", "pct_non_alphanumeric", "pct_date_time", "pct_unknown"};
//        String[] specificDatatypeLabels = {"pct_phones", "pct_email", "pct_url", "pct_ip", "pct_username", "pct_phrases", "pct_general",
//                "pct_date", "pct_time", "pct_date_time_specific", "pct_others"}; // Other = not determined
//
//        for (String datatypeLabel : datatypeLabels) columnFeatures.put(datatypeLabel, newFeatures.get(datatypeLabel));
//        for (String specificDatatypeLabel : specificDatatypeLabels) columnFeatures.put(specificDatatypeLabel, newFeatures.get(specificDatatypeLabel));

        Map<String, Object> newFeatures = generateLengths(conn, tableName, column);
        columnFeatures.put("len_max_word", newFeatures.get("len_max_word"));
        columnFeatures.put("len_min_word", newFeatures.get("len_min_word"));
        columnFeatures.put("len_avg_word", newFeatures.get("len_avg_word"));

        newFeatures = generateWordCount(conn, tableName, column);
        columnFeatures.put("words_cnt_max", newFeatures.get("words_cnt_max"));
        columnFeatures.put("words_cnt_min", newFeatures.get("words_cnt_min"));
        columnFeatures.put("words_cnt_avg", newFeatures.get("words_cnt_avg"));
        columnFeatures.put("number_words", newFeatures.get("number_words"));
        columnFeatures.put("words_cnt_sd", newFeatures.get("words_cnt_sd"));
    }

    public void addOtherFeatures(String column, Map<String, Object> columnFeatures) throws SQLException {
        Map<String, Object> newFeatures = generateFirstAndLastWord(conn, tableName, column);
        columnFeatures.put("first_word", newFeatures.get("first_word"));
        columnFeatures.put("last_word", newFeatures.get("last_word"));

        newFeatures = generateIsBinary(conn, column);
        columnFeatures.put("is_binary", newFeatures.get("is_binary"));

        newFeatures = generateIsEmpty(conn, tableName, column);
        columnFeatures.put("is_empty", newFeatures.get("is_empty"));
    }

    public static void generateAllProfilesOfAllDataInAFolder(String path, String pathToStore) throws Exception {
        Files.createDirectories(Path.of(pathToStore)); // Create target folder if ti does not exist

        // Path of the folder that contains the files to obtain profiles from (we get only the files)
        File[] files = (new File(path)).listFiles(File::isFile);
        assert files != null;

        int counter = 1;

        int parallelism = 8; // Number of available processors
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);

        for (File file : files) {
            int fileNumber = counter;
            executor.submit(() -> {
                try {
                    Connection conn = DuckDB.getConnection();
                    Profile p = new Profile(conn, "table" + fileNumber);
                    p.createProfile(String.valueOf(file), pathToStore);
                    System.out.println("File " + fileNumber + " out of " + files.length + ": " + file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            counter++;
        }
        executor.shutdown(); // Shutdown the executor when all tasks are completed
    }

    // Helper method to fix Unicode issue
    private static void fixUnicodeIssue(String dataPath) {
        try {
            Path path = Paths.get(dataPath);
            List<String> lines = Files.readAllLines(path, StandardCharsets.ISO_8859_1);
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
