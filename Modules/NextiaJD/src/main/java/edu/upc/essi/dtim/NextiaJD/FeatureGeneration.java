package edu.upc.essi.dtim.NextiaJD;

import org.apache.commons.codec.language.Soundex;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static edu.upc.essi.dtim.NextiaJD.Utils.getNumberOfValues;

public class FeatureGeneration {

    // Total number of rows of the dataset
    public static Double getNumberOfRows(Connection conn, String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM \"" + tableName + "\"");
        rs.next();
        return rs.getDouble(1);
    }

    static Map<String, Object> generateCardinalityFeatures(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();
        Double numberOfRows = getNumberOfRows(conn, tableName);
        Double numberOfValues = getNumberOfValues(conn, tableName, column);

        ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT \"" + column + "\") FROM \"" + tableName + "\"");
        rs.next();
        Double Cardinality = rs.getDouble(1);
        features.put("cardinality", Cardinality);
        features.put("uniqueness", (Cardinality / numberOfRows));
        features.put("incompleteness", (numberOfRows - numberOfValues)/numberOfRows);

        return features;
    }

    static Map<String, Object> generateEntropy(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        // Get entropy directly through DuckDB
        ResultSet rs = stmt.executeQuery("SELECT ENTROPY(\"" + column + "\") FROM \"" + tableName + "\"");
        rs.next();
        features.put("entropy", rs.getDouble(1));

        return features;
    }

    static Map<String, Object> generateFrequenciesAndPercentages(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        double numberOfRows = getNumberOfRows(conn, tableName);

        ResultSet rs = stmt.executeQuery(
                "SELECT AVG(count), MIN(count), MAX(count), stddev_pop(count), quantile_disc(count, 0.75) - quantile_disc(count, 0.25) " +
                        "FROM (SELECT COUNT(\"" + column + "\") AS count " +
                        "FROM \"" + tableName + "\" " +
                        "WHERE \"" + column + "\" IS NOT NULL " +
                        "GROUP BY \"" + column + "\")");
        while (rs.next()) {
            features.put("frequency_avg", rs.getDouble(1));
            features.put("frequency_min", rs.getDouble(2));
            features.put("frequency_max", rs.getDouble(3));
            features.put("frequency_sd", rs.getDouble(4));
            features.put("frequency_IQR", rs.getDouble(5)/numberOfRows);
            features.put("val_pct_min", rs.getDouble(2)/numberOfRows);
            features.put("val_pct_max", rs.getDouble(3)/numberOfRows);
            features.put("val_pct_std", rs.getDouble(4)/numberOfRows);
            features.put("constancy", rs.getDouble(3)/numberOfRows);
        }
        return features;
    }

    static Map<String, Object> generateFrequentWordContainment(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        // SQL Soundex does not work :(
        ResultSet rs = stmt.executeQuery(
                "SELECT \"" + column + "\", COUNT(\"" + column + "\") as count " +
                        "FROM \"" + tableName + "\" " +
                        "WHERE \"" + column + "\" IS NOT NULL " +
                        "GROUP BY \"" + column + "\" " +
                        "ORDER BY count desc, \"" + column + "\" asc LIMIT 10");
        LinkedList<String> frequentWords = new LinkedList<>();
        LinkedList<String> frequentWordsSoundex = new LinkedList<>();
        while (rs.next()) {
            frequentWords.add(rs.getString(1));
            String soundex = "";
            try {
                soundex = Soundex.US_ENGLISH.encode(rs.getString(1));
            }
            catch (Exception IllegalArgumentException) {
                soundex = "";
            }
            // If the string is purely numbers or non-alphanumeric characters, return the string itself
            if (soundex.isEmpty()) frequentWordsSoundex.add(rs.getString(1));
            else frequentWordsSoundex.add(soundex);
        }
        features.put("freqWordContainment", frequentWords);
        features.put("freqWordSoundexContainment", frequentWordsSoundex);

        return features;
    }

    static Map<String, Object> generateOctiles(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        double numberOfRows = getNumberOfRows(conn, tableName);

        ResultSet rs = stmt.executeQuery(
                "SELECT quantile_disc(count, 0.125), quantile_disc(count, 0.25), quantile_disc(count, 0.375), " +
                        "quantile_disc(count, 0.5), quantile_disc(count, 0.625), quantile_disc(count, 0.75), quantile_disc(count, 0.875) " +
                        "FROM (SELECT COUNT(\"" + column + "\") AS count " +
                        "FROM \"" + tableName + "\" " +
                        "WHERE \"" + column + "\" IS NOT NULL " +
                        "GROUP BY \"" + column + "\")");
        while (rs.next()) {
            features.put("frequency_1qo", rs.getInt(1)/numberOfRows);
            features.put("frequency_2qo", rs.getInt(2)/numberOfRows);
            features.put("frequency_3qo", rs.getInt(3)/numberOfRows);
            features.put("frequency_4qo", rs.getInt(4)/numberOfRows);
            features.put("frequency_5qo", rs.getInt(5)/numberOfRows);
            features.put("frequency_6qo", rs.getInt(6)/numberOfRows);
            features.put("frequency_7qo", rs.getInt(7)/numberOfRows);
        }

        return features;
    }

    static Map<String, Object> generateDatatypes(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        double numberOfRows = getNumberOfRows(conn, tableName);

        String datatype = null;
        String[] datatypeLabels = {"PctNumeric", "PctAlphanumeric", "PctAlphabetic", "PctNonAlphanumeric", "PctDateTime", "PctUnknown"};
        String specificDatatype = null;
        String[] specificDatatypeLabels = {"PctPhones", "PctEmail", "PctURL", "PctIP", "PctUsername", "PctPhrases", "PctGeneral",
                "PctDate", "PctTime", "PctDateTimeSpecific", "PctOthers"}; // Other = no-determined
        double[] datatypes = new double[6];
        double[] specificDatatypes = new double[11];

        ResultSet rs = stmt.executeQuery("SELECT \"" + column + "\" FROM \"" + tableName + "\"");
        while (rs.next()) {
            String string = rs.getString(1);
            if (string == null || string.equals("")) { ++datatypes[3]; ++specificDatatypes[10]; } // NonAlphanumeric & Other
            else if (string.equals(" ")) { ++datatypes[1]; ++specificDatatypes[6]; } // Alphanumeric & General
            else if (string.matches("^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]|(?:Jan|Mar|May|Jul|Aug|Oct|Dec)))" +
                    "\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2]|(?:Jan|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|" +
                    "Dec))\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)(?:0?2|(?:Feb))\\3(?:(?:(?:1[6-" +
                    "9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?" +
                    ":0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9]|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep))|(?:" +
                    "1[0-2]|(?:Oct|Nov|Dec)))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$")) { ++datatypes[4]; ++specificDatatypes[7]; } // Datetime & Date
            else if (string.matches("[-]?[0-9]+[,.]?[0-9]*([\\/][0-9]+[,.]?[0-9]*)*"))
            { ++datatypes[0]; ++specificDatatypes[10]; } // Numeric & Other
            else if (string.matches("^([a-z0-9_\\.\\+-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$"))
            { ++datatypes[1]; ++specificDatatypes[1]; } // Alphanumeric & Email
            else if (string.matches("^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5" +
                    "])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$"))
            { ++datatypes[1]; ++specificDatatypes[3]; } // Alphanumeric & IP
            else if (string.matches("^(?:(?:\\(?(?:00|\\+)([1-4]\\d\\d|[1-9]\\d?)\\)?)?[\\-\\.\\ \\\\\\/]?)" +
                    "?((?:\\(?\\d{1,}\\)?[\\-\\. \\\\\\/]?){0,})(?:[\\-\\.\\ \\\\\\/]?(?:#|ext\\.?|extensio" +
                    "n|x)[\\-\\.\\ \\\\\\/]?(\\d+))?$"))
            { ++datatypes[1]; ++specificDatatypes[8]; } // Alphanumeric & Phone
            else if (string.matches("^((([0]?[1-9]|1[0-2])(:|\\.)[0-5][0-9]((:|\\.)[0-5][0-9])?( )?(AM|am|aM|" +
                    "Am|PM|pm|pM|Pm))|(([0]?[0-9]|1[0-9]|2[0-3])(:|\\.)[0-5][0-9]((:|\\.)[0-5][0-9])?))$"))
            { ++datatypes[4]; ++specificDatatypes[1]; } // Datetime & Time
            else if (string.matches("^((((([13578])|(1[0-2]))[\\-\\/\\s]?(([1-9])|([1-2][0-9])|(3[01])))|" +
                    "((([469])|(11))[\\-\\/\\s]?(([1-9])|([1-2][0-9])|(30)))|(2[\\-\\/\\s]?(([1-9])|([1-2][0-9]" +
                    "))))[\\-\\/\\s]?\\d{4})(\\s((([1-9])|(1[02]))\\:([0-5][0-9])((\\s)|(\\:([0-5][0-9])\\s))([" +
                    "AM|PM|am|pm]{2,2})))?$"))
            { ++datatypes[4]; ++specificDatatypes[9]; } // Datetime & Datetime
            else if (string.matches("((mailto\\:|www\\.|(news|(ht|f)tp(s?))\\:\\/\\/){1}\\S+)"))
            { ++datatypes[1]; ++specificDatatypes[2]; } // Alphanumeric & URL
            else if (string.matches("^[a-zA-Z]+$")) {++datatypes[2]; ++specificDatatypes[10];} // Alphabetic & Other (otherST)
            else if (string.matches("^[a-z0-9_-]{3,16}$")) {++datatypes[1]; ++specificDatatypes[4];} // Alphanumeric & Username
            else if (string.matches("^[a-zA-Z0-9]*$")) {++datatypes[1]; ++specificDatatypes[6];} // Alphanumeric & General
            else if (string.matches("^[a-zA-Z0-9 ]*$")) {++datatypes[1]; ++specificDatatypes[5];} // Alphanumeric & Phrases
            else if (string.matches("[^\\s\\p{L}\\p{N}]+")) {++datatypes[3]; ++specificDatatypes[10];} // NonAlphanumeric & Other
            else {++datatypes[5]; ++specificDatatypes[10];} // Unknown & Other
        }

        double maxValue = 0;
        for (int i=0; i<datatypes.length; ++i) {
            if (datatypes[i] > maxValue) {
                datatype = datatypeLabels[i];
                maxValue = datatypes[i];
            }
        }
        maxValue = 0;
        for (int i=0; i<specificDatatypes.length; ++i) {
            if (specificDatatypes[i] > maxValue) {
                specificDatatype = specificDatatypeLabels[i];
                maxValue = specificDatatypes[i];
            }
        }

        features.put("datatype", datatype);
        for (int i=0; i<datatypes.length; ++i) features.put(datatypeLabels[i], datatypes[i]/numberOfRows);
        features.put("specificType", specificDatatype);
        for (int i=0; i<specificDatatypes.length; ++i) features.put(specificDatatypeLabels[i], specificDatatypes[i]/numberOfRows);

        return features;
    }

    static Map<String, Object> generateLengths(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        ResultSet rs = stmt.executeQuery(
                "SELECT MAX(max_string_length), MIN(min_string_length), AVG(avg_string_length) " +
                        "FROM (SELECT (list_aggregate(list_transform(str_split(\"" + column + "\", ' '), s -> LENGTH(s)), 'max')) AS max_string_length, " +
                        "(list_aggregate(list_transform(str_split(\"" + column + "\", ' '), s -> LENGTH(s)), 'min')) AS min_string_length, " +
                        "(list_aggregate(list_transform(str_split(\"" + column + "\", ' '), s -> LENGTH(s)), 'avg')) AS avg_string_length " +
                        "FROM \"" + tableName + "\" " +
                        "WHERE \"" + column + "\" IS NOT NULL)");
        while(rs.next()) {
            features.put("len_max_word", rs.getInt(1));
            features.put("len_min_word", rs.getInt(2));
            features.put("len_avg_word", rs.getInt(3));
        }

        return features;
    }

    static Map<String, Object> generateWordCount(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        ResultSet rs = stmt.executeQuery(
                "SELECT MAX(num_words), MIN(num_words), AVG(num_words), SUM(num_words), stddev_pop(num_words) " +
                        "FROM (SELECT LEN(\"" + column + "\") - LEN(REPLACE(\"" + column + "\", ' ', '')) + 1 AS num_words " +
                        "FROM \"" + tableName + "\" " +
                        "WHERE \"" + column + "\" IS NOT NULL)");
        while (rs.next()) {
            features.put("wordsCntMax", rs.getDouble(1));
            features.put("wordsCntMin", rs.getDouble(2));
            features.put("wordsCntAvg", rs.getDouble(3));
            features.put("numberWords", rs.getDouble(4));
            features.put("wordsCntSd", rs.getDouble(5));
        }
        return features;
    }

    static Map<String, Object> generateFirstAndLastWord(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        ResultSet rs = stmt.executeQuery(
                "SELECT FIRST(\"" + column + "\"), LAST(\"" + column + "\") " +
                        "FROM (SELECT * " +
                        "FROM \"" + tableName + "\" " +
                        "WHERE \"" + column + "\" IS NOT NULL " +
                        "ORDER BY (\"" + column + "\") ASC)");
        rs.next();
        features.put("firstWord", rs.getString(1));
        features.put("lastWord", rs.getString(2));

        return features;
    }

    static Map<String, Object> generateIsBinary(Connection conn, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        ResultSet rs = stmt.executeQuery("SELECT DATA_TYPE, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS");
        // WHERE COLUMN_NAME = \"" + column +  "\" does not work inside the sql query, so we filter outside of it
        while (rs.next()) {
            if (rs.getString(2).equals(column)) {
                if (rs.getString(1).equals("BOOLEAN")) features.put("binary", 1);
                else features.put("binary", 0);
            }
        }

        return features;
    }

    static Map<String, Object> generateIsEmpty(Connection conn, String tableName, String column) throws SQLException {
        Statement stmt = conn.createStatement();
        Map<String, Object> features = new HashMap<>();

        ResultSet rs = stmt.executeQuery("SELECT COUNT(\"" + column + "\") FROM \"" + tableName + "\" WHERE \"" + column + "\" IS NOT NULL" );
        rs.next();
        if (rs.getInt(1) == 0) features.put("isEmpty", 1);
        else features.put("isEmpty", 0);

        return features;
    }

}
