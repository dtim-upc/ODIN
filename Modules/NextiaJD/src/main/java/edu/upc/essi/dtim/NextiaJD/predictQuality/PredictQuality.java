package edu.upc.essi.dtim.NextiaJD.predictQuality;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.upc.essi.dtim.NextiaJD.utils.Utils.readCSVFile;

public class PredictQuality {
    LinkedList<String> metricsToNormalize = new LinkedList<>(Arrays.asList(
            "cardinality", "entropy", "frequency_avg", "frequency_min", "frequency_max", "frequency_sd",
            "len_max_word", "len_min_word", "len_avg_word", "words_cnt_max", "words_cnt_min", "words_cnt_avg",
            "number_words", "words_cnt_sd"
    ));
    Map<String, Integer> distancePattern = Map.<String, Integer>ofEntries(
            Map.entry("cardinality", 0), Map.entry("uniqueness", 0), Map.entry("entropy", 0),
            Map.entry("incompleteness", 0), Map.entry("frequency_avg", 0), Map.entry("frequency_min", 0),
            Map.entry("frequency_max", 0), Map.entry("frequency_sd", 0), Map.entry("val_pct_min", 0),
            Map.entry("val_pct_max", 0), Map.entry("val_pct_std", 0), Map.entry("constancy", 0),
            Map.entry("freq_word_containment", 1), Map.entry("freq_word_soundex_containment", 1), Map.entry("frequency_1qo", 0),
            Map.entry("frequency_2qo", 0), Map.entry("frequency_3qo", 0), Map.entry("frequency_4qo", 0),
            Map.entry("frequency_5qo", 0), Map.entry("frequency_6qo", 0), Map.entry("frequency_7qo", 0),
            Map.entry("pct_numeric", 0), Map.entry("pct_alphanumeric", 0), Map.entry("pct_alphabetic", 0),
            Map.entry("pct_non_alphanumeric", 0), Map.entry("pct_date_time", 0), Map.entry("pct_phones", 0),
            Map.entry("pct_email", 0), Map.entry("pct_url", 0), Map.entry("pct_ip", 0),
            Map.entry("pct_general", 0), Map.entry("pct_time", 0), Map.entry("pct_date", 0),
            Map.entry("pct_unknown", 0), Map.entry("pct_date_time_specific", 0), // with lowercase t for the specific type
            Map.entry("pct_username", 0), Map.entry("pct_phrases", 0), Map.entry("pct_others", 0),
            Map.entry("datatype", 2), Map.entry("specific_type", 2), Map.entry("len_max_word", 0),
            Map.entry("len_min_word", 0), Map.entry("len_avg_word", 0), Map.entry("words_cnt_max", 0),
            Map.entry("words_cnt_min", 0), Map.entry("words_cnt_avg", 0), Map.entry("number_words", 0),
            Map.entry("words_cnt_sd", 0), Map.entry("dataset_name", 2), Map.entry("attribute_name", 2),
            Map.entry("is_empty", 2), Map.entry("is_binary", 0), Map.entry("frequency_iqr", 0),
            Map.entry("first_word", 3), Map.entry("last_word", 3)
    );

    public PredictQuality() {}

    public double predictQuality(String path1, String path2, String att1, String att2) {
//        LinkedList<Map<String, Object>> profiles1 = readJSONFile(path1);
//        LinkedList<Map<String, Object>> profiles2 = readJSONFile(path2);
        LinkedList<Map<String, Object>> profiles1 = readCSVFile(path1);
        LinkedList<Map<String, Object>> profiles2 = readCSVFile(path2);

        // Remove null rows and normalize the profiles
        profiles1.removeAll(Collections.singleton(null));
        profiles2.removeAll(Collections.singleton(null));
        normalizeProfile(profiles1);
        normalizeProfile(profiles2);

        // Get the profiles that we need (that is, get only the two profiles corresponding to the two attributes to compare)
        Map<String, Object> profile1 = new HashMap<>();
        Map<String, Object> profile2 = new HashMap<>();
        for (Map<String, Object> profile: profiles1) {
            if (profile.get("attribute_name").equals(att1)) profile1 = profile;
        }
        for (Map<String, Object> profile: profiles2) {
            if (profile.get("attribute_name").equals(att2)) profile2 = profile;
        }

        // Calculate the distances
        Map<String, Object> distances = calculateDistances(profile1, profile2);
        if (distances.isEmpty()) {
            throw new RuntimeException();
        }
//        writeDistances("C:\\Projects\\ODIN", distances, true);

        return predictQualityThroughModel(distances);
    }

    private void writeDistances(String distancesFilePath, Map<String, Object> distances, Boolean writeHeader) throws IOException {
        File file = new File(distancesFilePath.replace("/", "_").replace(": ","_"));
        boolean fileExists = file.exists();
        Writer writer = new FileWriter(file, true);

        if (!fileExists) {
            for (String key: distances.keySet()) {
                writer.write(key);
                writer.write(",");
            }
            writer.write("\n");
        }

        for (String key: distances.keySet()) {
            writer.write(String.valueOf(distances.get(key)));
            writer.write(",");
        }
        writer.write("\n");
        writer.flush();
    }

    private void writeHeader(String distancesFilePath, Map<String, Object> distances) throws IOException {
        File file = new File(distancesFilePath.replace("/", "_"));
        Writer writer = new FileWriter(file, true);

        for (String key: distances.keySet()) {
            writer.write(key);
            writer.write(",");
        }
        writer.write("\n");
        writer.flush();
    }

    private double predictQualityThroughModel(Map<String, Object> distances) {
        return 0.0;
    }

    private Map<String, Object> calculateDistances(Map<String, Object> profile1, Map<String, Object> profile2) {
        Map<String, Object> distances = new HashMap<>();
        try {
            for (String feature: profile1.keySet()) {
                if (distancePattern.get(feature) == 0) { // subtraction for most numeric values, such as cardinality
                    double value = objectToDouble(profile1.get(feature)) - objectToDouble(profile2.get(feature));
                    distances.put(feature, value);
                }
                else if (distancePattern.get(feature) == 1) { // containment for arrays, such as the most common words
                    List<String> elementsList1 = Arrays.asList(((String) profile1.get(feature)).replaceAll("\\[|\\]|\\s", "").split(","));
                    List<String> elementsList2 = Arrays.asList(((String) profile2.get(feature)).replaceAll("\\[|\\]|\\s", "").split(","));
                    LinkedList<String> listValues1 = new LinkedList<>(elementsList1);
                    LinkedList<String> listValues2 = new LinkedList<>(elementsList2);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (distances.size() != 40 && distances.size() != 61) { // 61 for full
            distances = new HashMap<>();
        }

        return distances;
    }

    private Map<String,Object> calculateBinaryFeatures(Map<String, Object> profile1, Map<String, Object> profile2) {
        Map<String, Object> binaryFeatures = new HashMap<>();
        Double levDistance = Double.valueOf(LevenshteinDistance.getDefaultInstance()
                .apply((CharSequence) profile1.get("attribute_name"), (CharSequence) profile2.get("attribute_name")));
        binaryFeatures.put("name_dist", levDistance);

        return binaryFeatures;
    }

    private void normalizeProfile(LinkedList<Map<String, Object>> profile) {
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
    }

    private double objectToDouble(Object o) {
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

    public void calculateDistancesForAllProfilesInAFolder(String path, String distancesPath) {
        File[] files = (new File (path)).listFiles(File::isFile);
        boolean writeHeader = true;

        assert files != null;
        for (int i = 0; i < files.length; ++i) {
            System.out.println("Started iteration " + i);
            for (int j = 0; j< files.length; ++j) {
                if (j > i) {
                    LinkedList<Map<String, Object>> profiles1 = readCSVFile(String.valueOf(files[i]));
                    LinkedList<Map<String, Object>> profiles2 = readCSVFile(String.valueOf(files[j]));

                    // Remove null rows and normalize the profiles
                    profiles1.removeAll(Collections.singleton(null));
                    profiles2.removeAll(Collections.singleton(null));
                    normalizeProfile(profiles1);
                    normalizeProfile(profiles2);

                    // For every attribute of every dataset in combination of every other attribute of every other dataset,
                    // we get the profiles of both attributes and calculate the distances.
                    for (Map<String, Object> profile1: profiles1) {
                        for (Map<String, Object> profile2: profiles2) {
                            Map<String, Object> distances = calculateDistances(profile1, profile2);
                            if (!distances.isEmpty()) {
                                try {
                                    writeDistances(distancesPath + "\\distances.csv", distances, writeHeader);
                                    writeHeader = false;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    public void calculateDistancesAttVsFolder(String attribute, String dataset, String path) {
        File[] files = (new File (path)).listFiles(File::isFile);

        LinkedList<Map<String, Object>> profilesDataset = readCSVFile(path + "\\" + dataset);
        profilesDataset.removeAll(Collections.singleton(null));
        normalizeProfile(profilesDataset);
        profilesDataset.forEach(prof -> System.out.println(prof.get("attribute_name")));
        Map<String, Object> queryProfile = profilesDataset.stream().filter(prof -> prof.get("attribute_name").equals("\"" + attribute.trim() + "\"")).findFirst().orElse(null);
        assert queryProfile != null;
        assert files != null;

        ExecutorService executor = Executors.newFixedThreadPool(8);

        for (int i = 0; i < files.length; ++i) {
            int finalI = i;
            String fileDatasetName = String.valueOf(files[finalI]).substring(String.valueOf(files[finalI]).lastIndexOf("\\") + 1);
            if (i == 0) {
                // Filter out the same dataset
                if (!fileDatasetName.equals(dataset)) {
                    LinkedList<Map<String, Object>> dataLakeProfiles = readCSVFile(String.valueOf(files[finalI]));
                    for (Map<String, Object> dataLakeProfile : dataLakeProfiles) {
                        Map<String, Object> distances = calculateDistances(queryProfile, dataLakeProfile);
                        if (!distances.isEmpty()) {
                            try {
                                File folder = new File(path + "\\distances"); // Create folder
                                folder.mkdirs();

                                String distancesPath = path + "\\distances\\" + "distances_" + dataset.replace(".csv", "") + "_" + attribute + ".csv";
                                writeDistances(distancesPath, distances, false);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            else {
                executor.submit(() -> {
                    // Filter out the same dataset
//                    if (!fileDatasetName.equals(dataset)) {
                        LinkedList<Map<String, Object>> dataLakeProfiles = readCSVFile(String.valueOf(files[finalI]));
                        for (Map<String, Object> dataLakeProfile : dataLakeProfiles) {
                            Map<String, Object> distances = calculateDistances(queryProfile, dataLakeProfile);
                            if (!distances.isEmpty()) {
                                try {
                                    File folder = new File(path + "\\distances"); // Create folder
                                    folder.mkdirs();

                                    String distancesPath = path + "\\distances\\" + "distances_" + dataset.replace(".csv", "") + "_" + attribute + ".csv";
                                    writeDistances(distancesPath, distances, false);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
//                    }
                });
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
