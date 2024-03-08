package edu.upc.essi.dtim.NextiaJD;

import edu.upc.essi.dtim.NextiaJD.predictQuality.PredictQuality;
import edu.upc.essi.dtim.NextiaJD.predictQuality.Profile;
import edu.upc.essi.dtim.NextiaJD.utils.DuckDB;

import java.sql.Connection;

import static edu.upc.essi.dtim.NextiaJD.predictQuality.Profile.generateAllProfilesOfAllDataInAFolder;

public class Main {
    public static void main(String[] args)  {
//        Connection conn = DuckDB.getConnection();
//        Profile p = new Profile(conn);
//        // if pathToStoreProfile is left blank (i.e. "") the profile will not be stored in disk
//        // if resultingProfileName is left blank (i.e. "") the profile file name will be the same as the original csv
//        p.createProfile("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\Profiles", "test2");
//        p.createProfile("D:\\Work\\TFM\\Others\\eo_xx.csv", "", "");


//        CalculateQuality cq = new CalculateQuality(conn, 4.0, 1);
//        System.out.println(cq.calculateQualityDiscrete("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\TFM\\Others\\eo_xx.csv", "ein", "ein"));
//        System.out.println(cq.calculateQualityContinuous("D:\\Work\\TFM\\Others\\eo_xx.csv", "D:\\Work\\TFM\\Others\\eo_xx.csv", "ein", "ein"));

//        PredictQuality pq = new PredictQuality(conn);
//        pq.predictQuality("D:\\Projects\\Files\\eo4_profile.json", "D:\\Projects\\Files\\eo_xx_2_profile.json", "NAME", "NAME");

        try {
//            generateAllProfilesOfAllDataInAFolder("C:\\Work\\NextiaJD\\datasets", "C:\\Work\\NextiaJD\\datasets\\profilesCSV");
            Connection conn = DuckDB.getConnection();
            Profile p = new Profile(conn);
            PredictQuality pq = new PredictQuality(conn);

//            p.createProfile("C:\\Work\\NextiaJD\\datasets\\us_companies_copy.csv", "C:\\Work\\NextiaJD");

            pq.calculateDistancesForAllProfilesInAFolder("C:\\Work\\NextiaJD\\datasets\\profilesCSV", "C:\\Work\\NextiaJD");

//            pq.predictQuality("C:\\Work\\NextiaJD\\datasets\\profilesCSV\\acquisitions_profile.csv", "C:\\Work\\NextiaJD\\datasets\\profilesCSV\\acquisitions_profile.csv",
//                    "AcquisitionID", "AcquisitionID");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}