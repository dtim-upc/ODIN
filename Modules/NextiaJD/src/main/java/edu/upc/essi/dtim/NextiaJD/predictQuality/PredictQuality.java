package edu.upc.essi.dtim.NextiaJD.predictQuality;

import jakarta.xml.bind.JAXBException;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import java.io.*;
import java.sql.Connection;
import java.util.*;

import static edu.upc.essi.dtim.NextiaJD.utils.Utils.writeJSON;

public class PredictQuality {

    Connection conn;
    LinkedList<String> metricsToNormalize = new LinkedList<>(Arrays.asList(
            "cardinality", "entropy", "frequency_avg", "frequency_min", "frequency_max", "frequency_sd",
            "len_max_word", "len_min_word", "len_avg_word", "wordsCntMax", "wordsCntMin", "wordsCntAvg",
            "numberWords", "wordsCntSd"
    ));
    Map<String, Integer> distancePattern = Map.<String, Integer>ofEntries(
            Map.entry("cardinality", 0), Map.entry("uniqueness", 0), Map.entry("entropy", 0),
            Map.entry("incompleteness", 0), Map.entry("frequency_avg", 0), Map.entry("frequency_min", 0),
            Map.entry("frequency_max", 0), Map.entry("frequency_sd", 0), Map.entry("val_pct_min", 0),
            Map.entry("val_pct_max", 0), Map.entry("val_pct_std", 0), Map.entry("constancy", 0),
            Map.entry("freqWordContainment", 1), Map.entry("freqWordSoundexContainment", 1), Map.entry("frequency_1qo", 0),
            Map.entry("frequency_2qo", 0), Map.entry("frequency_3qo", 0), Map.entry("frequency_4qo", 0),
            Map.entry("frequency_5qo", 0), Map.entry("frequency_6qo", 0), Map.entry("frequency_7qo", 0),
            Map.entry("PctNumeric", 0), Map.entry("PctAlphanumeric", 0), Map.entry("PctAlphabetic", 0),
            Map.entry("PctNonAlphanumeric", 0), Map.entry("PctDateTime", 0), Map.entry("PctPhones", 0),
            Map.entry("PctEmail", 0), Map.entry("PctURL", 0), Map.entry("PctIP", 0),
            Map.entry("PctGeneral", 0), Map.entry("PctTime", 0), Map.entry("PctDate", 0),
            Map.entry("PctUnknown", 0), Map.entry("PctDatetime", 0), // with lowercase t for the specific type
            Map.entry("PctUsername", 0), Map.entry("PctPhrases", 0), Map.entry("PctOthers", 0),
            Map.entry("datatype", 2), Map.entry("specificType", 2), Map.entry("len_max_word", 0),
            Map.entry("len_min_word", 0), Map.entry("len_avg_word", 0), Map.entry("wordsCntMax", 0),
            Map.entry("wordsCntMin", 0), Map.entry("wordsCntAvg", 0), Map.entry("numberWords", 0),
            Map.entry("wordsCntSd", 0), Map.entry("ds_name", 2), Map.entry("att_name", 2),
            Map.entry("isEmpty", 2), Map.entry("binary", 0), Map.entry("frequency_IQR", 0),
            Map.entry("firstWord", 3), Map.entry("lastWord", 3)
    );

    public PredictQuality(Connection conn) {this.conn = conn;}

    public double predictQuality(String path1, String path2, String att1, String att2) throws IOException, ParseException, JAXBException, SAXException {
        LinkedList<Map<String, Object>> profiles1 = readJSONFile(path1);
        LinkedList<Map<String, Object>> profiles2 = readJSONFile(path2);
        // Remove null rows
        profiles1.removeAll(Collections.singleton(null));
        profiles2.removeAll(Collections.singleton(null));

        Map<String, Object> distances = getCardinalityProportion(profiles1, att1, profiles2, att2);

        profiles1 = normalizeProfile(profiles1);
        profiles2 = normalizeProfile(profiles2);

        Map<String, Object> profile1 = new HashMap<>();
        Map<String, Object> profile2 = new HashMap<>();
        for (Map<String, Object> profile: profiles1) {
            if (profile.get("att_name").equals(att1)) profile1 = profile;
        }
        for (Map<String, Object> profile: profiles2) {
            if (profile.get("att_name").equals(att2)) profile2 = profile;
        }

        distances.putAll(calculateDistances(profile1, profile2));
        writeDistances(distances);

        return predictQualityThroughModel(distances);
    }

    private void writeDistances(Map<String, Object> distances) throws IOException {
        List<Map<String, Object>> listOfMap = new LinkedList<>();
        listOfMap.add(distances);

        File file = new File("distances.csv"); // NextiaJD2/distances.csv
        Writer writer = new FileWriter(file, true);
//        CsvSchema schema = null;
//        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
//        if (listOfMap != null && !listOfMap.isEmpty()) {
//            for (String col : listOfMap.get(0).keySet()) {
//                schemaBuilder.addColumn(col);
//            }
//            schema = schemaBuilder.build().withLineSeparator(System.lineSeparator()).withHeader();
//        }
//        CsvMapper mapper = new CsvMapper();
//        mapper.writer(schema).writeValues(writer).writeAll(listOfMap);
        for (String key: distances.keySet()) {
            writer.write(String.valueOf(distances.get(key)));
            writer.write(",");
        }
        writer.write("\n");
        writer.flush();
    }

    private Map<String, Object> getCardinalityProportion(LinkedList<Map<String, Object>> profiles1, String att1, LinkedList<Map<String, Object>> profiles2, String att2) {
        Map<String, Object> distances = new HashMap<>();

        double cardinality1 = 0.0;
        double cardinality2 = 0.0;
        for (Map<String, Object> profile: profiles1) {
            if (profile.get("att_name").equals(att1)) cardinality1 = Double.parseDouble(String.valueOf(profile.get("cardinality")));
        }
        for (Map<String, Object> profile: profiles2) {
            if (profile.get("att_name").equals(att2)) cardinality2 = Double.parseDouble(String.valueOf(profile.get("cardinality")));
        }
        distances.put("K", Math.min(cardinality1, cardinality2)/Math.max(cardinality1, cardinality2));
        distances.put("cardinalityRaw", cardinality1);
        distances.put("cardinalityRaw_2", cardinality2);

        return distances;
    }

    private double predictQualityThroughModel(Map<String, Object> distances) throws IOException, JAXBException, SAXException {
//        String filePath = new File("").getAbsolutePath();
//        filePath = filePath.concat("\\src\\main\\resources\\model\\ML_Best_model.pmml");
//        Path modelPath = Paths.get("C:\\Projects\\NextiaJD2\\src\\main\\resources\\model\\ML_Best_model.pmml");
//        System.out.println(distances);
//
//        Evaluator evaluator = new LoadingModgetNumberOfValueselEvaluatorBuilder().load(modelPath.toFile()).build();
//        evaluator.verify();
//
//        FieldName targetName = evaluator.getTargetFields().get(0).getName();
//        List<InputField> inputFields = evaluator.getInputFields();
//
//        // PctSpaces, freqWordCLEANContainment (the other containments are okay)
//        distances.put("freqWordCleanContainment", 0.5);
//        distances.put("PctSpaces", 0.0);
//
//        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
//        for (InputField inputField : inputFields) {
//            FieldName inputName = inputField.getName();
//            Object value = distances.get(inputName.toString());
//            FieldValue inputValue = inputField.prepare(value);
//            arguments.put(inputName, inputValue);
//        }
//
//        // Evaluating the model with known-good arguments
//        Map<FieldName, ?> results = evaluator.evaluate(arguments);
//
//        // Extracting prediction
//        Map<String, ?> resultRecord = EvaluatorUtil.decodeAll(results);
//        Double yPred = (Double) resultRecord.get(targetName.toString());
//        System.out.println("Prediction is " + yPred);

        return 0.0;
    }

    private Map<String, Object> calculateDistances(Map<String, Object> profile1, Map<String, Object> profile2) {
        Map<String, Object> distances = new HashMap<>();
        for (String feature: profile1.keySet()) {
            if (distancePattern.get(feature) == 0) { // subtraction for most numeric values, such as cardinality
                double value = objectToDouble(profile1.get(feature)) - objectToDouble(profile2.get(feature));
                distances.put(feature, value);
            }
            else if (distancePattern.get(feature) == 1) { // containment for arrays, such as the most common words
                LinkedList<String> listValues1 = new LinkedList((Collection) profile1.get(feature));
                LinkedList<String> listValues2 = new LinkedList((Collection) profile2.get(feature));

                double numberOfContainedValues = 0.0;
                for (String value: listValues1) {
                    if (listValues2.contains(value)) numberOfContainedValues += 1;
                }
                distances.put(feature, numberOfContainedValues/listValues1.size());
            }
            else if (distancePattern.get(feature) == 2) { // add both values, such as the two datasets names
                distances.put(feature, profile1.get(feature));
                distances.put(feature + "_2", profile2.get(feature));
            }
            else if (distancePattern.get(feature) == 3) { // levenshtein distance, such as for the first words
                distances.put(feature,  Double.valueOf(LevenshteinDistance.getDefaultInstance()
                        .apply((CharSequence) profile1.get(feature), (CharSequence) profile2.get(feature))));
            }
        }

        distances.putAll(calculateBinaryFeatures(profile1,profile2));

        return distances;
    }

    private Map<String,Object> calculateBinaryFeatures(Map<String, Object> profile1, Map<String, Object> profile2) {
        Map<String, Object> binaryFeatures = new HashMap<>();
        Double levDistance = Double.valueOf(LevenshteinDistance.getDefaultInstance()
                .apply((CharSequence) profile1.get("att_name"), (CharSequence) profile2.get("att_name")));
        binaryFeatures.put("name_dist", levDistance);

        return binaryFeatures;
    }

    private LinkedList<Map<String, Object>> normalizeProfile(LinkedList<Map<String, Object>> profile) throws IOException {
        double numberOfColumns = profile.size();
        Set<String> keySet = profile.get(0).keySet();
        // z-score
        for (String key: keySet) {
            if (metricsToNormalize.contains(key)) {
                LinkedList<Double> values = new LinkedList<>();
                for (Map<String, Object> metrics: profile) {
                    values.add(objectToDouble(metrics.get(key)));
                }
                double sum = 0.0;
                for (Double value: values) {
                    sum += value;
                }
                double mean = sum/numberOfColumns;
                double variance  = 0.0;
                for (Double value: values) {
                    variance += Math.pow(value - mean, 2);
                }
                double standardDeviation = 0.0;
                if (numberOfColumns != 1) {
                    standardDeviation = Math.sqrt(variance/(numberOfColumns - 1));
                }
                if (standardDeviation != 0.0) {
                    for (Map<String, Object> metrics: profile) {
                        double newValue = (objectToDouble(metrics.get(key)) - mean)/standardDeviation;
                        metrics.put(key, newValue);
                    }
                }
                else {
                    for (Map<String, Object> metrics: profile) {
                        metrics.put(key, 0);
                    }
                }
            }
        }
        writeJSON(profile, "", "/home/marc/Escritorio/Files/Profiles", "normalized_profile");
        return profile;
    }

    private double objectToDouble(Object o) {
        return Double.parseDouble(String.valueOf(o));
    }

    private LinkedList<Map<String, Object>> readJSONFile(String path) throws IOException, ParseException {
        JSONParser jsonParser  = new JSONParser();
        FileReader reader = new FileReader(path);
        Object obj = jsonParser.parse(reader);
        JSONArray profileJSON = (JSONArray) obj;
        LinkedList<Map<String,Object>> profile = new LinkedList<>();

        for (Object featureJSON : profileJSON) {
            Map<String, Object> features = (Map<String, Object>) featureJSON;
            profile.add(features);
        }

        return profile;
    }
}
