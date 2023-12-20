package edu.upc.essi.dtim.NextiaJD.predictQuality;

import edu.upc.essi.dtim.NextiaJD.utils.DuckDB;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.upc.essi.dtim.NextiaJD.predictQuality.FeatureGeneration.*;
import static edu.upc.essi.dtim.NextiaJD.utils.Utils.getNumberOfValues;
import static edu.upc.essi.dtim.NextiaJD.utils.Utils.preprocessing;

public class Profile {

    Connection conn;
    String tableName = "temptTable";

    public Profile(Connection conn) {
        this.conn = conn;
    }

    public JSONArray createProfile(String path, String pathToStoreProfile, String resultingProfileName) throws SQLException, IOException {
        // Create table from file and preprocess the data
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE \"" + tableName + "\" AS SELECT * FROM read_csv_auto('" + path + "', header=True, all_varchar=True, ignore_errors=True)");
        preprocessing(conn, tableName);

        // Generate the profile of the table: for each column, its profile is generated and added to the features variable
        LinkedList<Map<String,Object>> features = new LinkedList<>();
        ResultSet rs = stmt.executeQuery("SELECT COLUMN_NAME, TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS");
        while (rs.next()) {
            if (rs.getString(2).equals(tableName)) {
                // We only generate the profile if the column has some value (i.e. if it is only null values, we do not create the profile)
                if (getNumberOfValues(conn, tableName, rs.getString(1)) != 0.0) {
                    features.add(createProfileOfColumn(rs.getString(1)));
                }
            }
        }

        // Add name of the dataset to each column
        for (Map<String,Object> map: features) {
            map.put("ds_name", Paths.get(path).getFileName().toString());
        }

        // Write the profile in a JSON file (if needed)
        if (!pathToStoreProfile.isEmpty()) {
            writeJSON(features, path, pathToStoreProfile, resultingProfileName);
        }

        // Return the JSON profile to the process that invokes the function
        JSONArray json = new JSONArray();
        json.addAll(features);
        stmt.execute("DROP TABLE \"" + tableName + "\"");
        return json;
    }

    public void writeJSON(LinkedList<Map<String, Object>> features, String path, String pathToStoreProfile, String resultingProfileName) throws IOException {
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

    public Map<String,Object> createProfileOfColumn(String column) throws SQLException {
        Map<String, Object> columnFeatures = new HashMap<>();
        addCardinalityFeatures(column, columnFeatures);
        addValueDistributionFeatures(column, columnFeatures);
        addSyntacticFeatures(column, columnFeatures);
        addOtherFeatures(column, columnFeatures);
        columnFeatures.put("att_name", column); // Add name of the column
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
        columnFeatures.put("frequency_IQR", newFeatures.get("frequency_IQR"));
        columnFeatures.put("val_pct_min", newFeatures.get("val_pct_min"));
        columnFeatures.put("val_pct_max", newFeatures.get("val_pct_max"));
        columnFeatures.put("val_pct_std", newFeatures.get("val_pct_std"));
        columnFeatures.put("constancy", newFeatures.get("constancy"));

        newFeatures = generateFrequentWordContainment(conn, tableName, column);
        columnFeatures.put("freqWordContainment", newFeatures.get("freqWordContainment"));
        columnFeatures.put("freqWordSoundexContainment", newFeatures.get("freqWordSoundexContainment"));

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
        Map<String, Object> newFeatures = generateDatatypes(conn, tableName, column);
        columnFeatures.put("datatype", newFeatures.get("datatype"));
        columnFeatures.put("specificType", newFeatures.get("specificType"));

        String[] datatypeLabels = {"PctNumeric", "PctAlphanumeric", "PctAlphabetic", "PctNonAlphanumeric", "PctDateTime", "PctUnknown"};
        String[] specificDatatypeLabels = {"PctPhones", "PctEmail", "PctURL", "PctIP", "PctUsername", "PctPhrases", "PctGeneral",
                "PctDate", "PctTime", "PctDateTimeSpecific", "PctOthers"};

        for (String datatypeLabel : datatypeLabels) columnFeatures.put(datatypeLabel, newFeatures.get(datatypeLabel));
        for (String specificDatatypeLabel : specificDatatypeLabels) columnFeatures.put(specificDatatypeLabel, newFeatures.get(specificDatatypeLabel));

        newFeatures = generateLengths(conn, tableName, column);
        columnFeatures.put("len_max_word", newFeatures.get("len_max_word"));
        columnFeatures.put("len_min_word", newFeatures.get("len_min_word"));
        columnFeatures.put("len_avg_word", newFeatures.get("len_avg_word"));

        newFeatures = generateWordCount(conn, tableName, column);
        columnFeatures.put("wordsCntMax", newFeatures.get("wordsCntMax"));
        columnFeatures.put("wordsCntMin", newFeatures.get("wordsCntMin"));
        columnFeatures.put("wordsCntAvg", newFeatures.get("wordsCntAvg"));
        columnFeatures.put("numberWords", newFeatures.get("numberWords"));
        columnFeatures.put("wordsCntSd", newFeatures.get("wordsCntSd"));
    }

    public void addOtherFeatures(String column, Map<String, Object> columnFeatures) throws SQLException {
        Map<String, Object> newFeatures = generateFirstAndLastWord(conn, tableName, column);
        columnFeatures.put("firstWord", newFeatures.get("firstWord"));
        columnFeatures.put("lastWord", newFeatures.get("lastWord"));

        newFeatures = generateIsBinary(conn, column);
        columnFeatures.put("binary", newFeatures.get("binary"));

        newFeatures = generateIsEmpty(conn, tableName, column);
        columnFeatures.put("isEmpty", newFeatures.get("isEmpty"));
    }

    public static void generateAllProfilesOfAllDataInAFolder(String path, String pathToStore) throws Exception {
        Connection conn = DuckDB.getConnection();
        // Path of the folder that contains the files to obtain profiles from
        Set<String> listOfFiles = Stream.of(Objects.requireNonNull(new File(path).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
        int counter = 1;
        for (String file: listOfFiles) {
            System.out.println("File " + counter + " out of " + listOfFiles.size());
            if (counter > 225) { // This if is to place conditions to isolate some of the files
                Profile p = new Profile(conn);
                if (!file.equals(".DS_Store")) {
                    JSONArray profile1 = p.createProfile(path + "/" + file, pathToStore, "");
                }
            }
            counter++;
        }
    }

}
