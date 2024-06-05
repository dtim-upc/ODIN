package edu.upc.essi.dtim.NextiaJD;

import com.opencsv.CSVWriter;
import edu.upc.essi.dtim.NextiaJD.predictQuality.PredictQuality;
import edu.upc.essi.dtim.NextiaJD.predictQuality.Profile;
import edu.upc.essi.dtim.NextiaJD.utils.DuckDB;
import org.apache.commons.lang3.tuple.Pair;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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

//        PredictQuality pq = new PredictQuality();
//        pq.predictQuality("D:\\Projects\\Files\\eo4_profile.json", "D:\\Projects\\Files\\eo_xx_2_profile.json", "NAME", "NAME");

//        pq.calculateDistancesAttVsFolder("dummy_value", "file_1_profile.csv", "C:\\Users\\marc.maynou\\Desktop\\scalability\\linearity\\1_gb\\profiles");
//        pq.calculateDistancesForAllProfilesInAFolder("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\profiles_copy", "C:\\Users\\marc.maynou\\Desktop");

//        pq.predictQuality("C:\\Work\\NextiaJD\\datasets\\profilesCSV\\acquisitions_profile.csv", "C:\\Work\\NextiaJD\\datasets\\profilesCSV\\acquisitions_profile.csv",
//                    "AcquisitionID", "AcquisitionID");

//        santos();
//        tus();
//        santosBig();
//        nextiaJD();
//        tusBig();
//        scalability();
        d3l();

//        p.createProfile("C:\\Work\\NextiaJD\\nextia\\datasets\\worldcitiespop.csv", "C:\\Work\\NextiaJD\\nextia");

//        p.createProfile(args[0], args[1]);

//        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\D3L\\benchmark", "C:\\Work\\NextiaJD\\other_datasets\\D3L\\profiles_short");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

    }

    public static void scalability() {
        try {
            PredictQuality pq = new PredictQuality();

            for (int i = 1; i <= 20; ++i) {
                pq.calculateDistancesAttVsFolder("dummy_value", "file_" + i + "_profile.csv", "C:\\Users\\marc.maynou\\Desktop\\scalability\\sizes\\size_100_kb\\profiles");
                System.out.println("Query column " + i + " out of " + 20);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void santos() {
        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\datalake", "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\profiles");
            PredictQuality pq = new PredictQuality();

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\santos_small_benchmark_groundtruth.csv"))) {
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

            int counter = 1;

            for (Pair<String, String> pair: listOfQueryColumns) {
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\profiles_short");
                System.out.println("Query column " + counter + " out of " + listOfQueryColumns.size());
                counter++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void tus() {
        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\tus\\tus_small\\csvfiles", "C:\\Work\\NextiaJD\\other_datasets\\tus\\tus_small\\profiles");
            Connection conn = DuckDB.getConnection();
            PredictQuality pq = new PredictQuality();

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader  reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\tus_small\\TUS_benchmark_relabeled_groundtruth.csv"))) {
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

            int counter = 1;

            for (Pair<String, String> pair: listOfQueryColumns) {
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\other_datasets\\tus_small\\profiles_short");
                System.out.println("Query column " + counter + " out of " + listOfQueryColumns.size());
                counter++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void santosBig() {
        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_big\\datalake", "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_big\\xd");
            PredictQuality pq = new PredictQuality();

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader  reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_big\\santos_big_benchmark_groundtruth.csv"))) {
                String[] headerLine = reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) {
                    String dataset = line[0];
                    String attribute = line[2];
                    if (!listOfQueryColumns.contains(Pair.of(dataset, attribute))) {
                        listOfQueryColumns.add(Pair.of(dataset, attribute));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            int counter = 1;

            for (Pair<String, String> pair: listOfQueryColumns) {
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_big\\profiles_short");
                System.out.println("Query column " + counter + " out of " + listOfQueryColumns.size());
                ++counter;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void nextiaJD() {
        try {
            PredictQuality pq = new PredictQuality();

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader  reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\nextia\\ground_truth_validate.csv"))) {
                String[] headerLine = reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) {
                    String dataset = line[0];
                    String attribute = line[1];
                    if (!listOfQueryColumns.contains(Pair.of(dataset, attribute))) {
                        listOfQueryColumns.add(Pair.of(dataset, attribute));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            int counter = 1;


            for (Pair<String, String> pair: listOfQueryColumns) {
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\nextia\\profiles");
                System.out.println("Query column " + counter + " out of " + listOfQueryColumns.size());
                ++counter;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void tusBig() {
        try {
            PredictQuality pq = new PredictQuality();

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\tus_big\\TUS_large_candidate_queries_sample.csv"))) {
                String[] headerLine = reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) {
                        String dataset = line[0];
                        String attribute = line[1];
                        if (!listOfQueryColumns.contains(Pair.of(dataset, attribute))) {
                            listOfQueryColumns.add(Pair.of(dataset, attribute));
                        }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int counter = 0;
            for (Pair<String, String> pair: listOfQueryColumns) {
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft().replace(".csv", "_profile.csv"), "C:\\Work\\NextiaJD\\other_datasets\\tus_big\\profiles_short");
                System.out.println("Query column " + counter + " out of " + listOfQueryColumns.size());
                ++counter;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void d3l() {
        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\datalake", "C:\\Work\\NextiaJD\\other_datasets\\santos_benchmark_small\\profiles");
            PredictQuality pq = new PredictQuality();

            List<Pair<String,String>> listOfQueryColumns = new LinkedList<>();
            try (CSVReader reader = new CSVReader(new FileReader("C:\\Work\\NextiaJD\\other_datasets\\D3L\\d3l_ground_truth_sample.csv"))) {
                String[] headerLine = reader.readNext();
                String[] line;
                while ((line = reader.readNext()) != null) {
                    String dataset = line[1];
                    String attribute = line[2];
                    if (!listOfQueryColumns.contains(Pair.of(dataset, attribute))) {
                        listOfQueryColumns.add(Pair.of(dataset, attribute));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            int counter = 1;

            for (Pair<String, String> pair: listOfQueryColumns) {
                pq.calculateDistancesAttVsFolder(pair.getRight(), pair.getLeft() + "_profile.csv", "C:\\Work\\NextiaJD\\other_datasets\\D3L\\profiles_short");
                System.out.println("Query column " + counter + " out of " + listOfQueryColumns.size());
                counter++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}