package edu.upc.essi.dtim.NextiaCD;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

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
                break;
            case "computeDistances":
                if (args.length != 5) {
                    System.out.println("4 parameters required: (1) the name of the dataset that contains the query column, (2) the query column name");
                    System.out.println("(3) the path to the list of profiles to compute distances with and (4) the path to store the resulting distances");
                    break;
                }
                System.out.println("Computing the distances");
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
                }
                else {
                }
                break;
            default:
                System.out.println("Unknown function: " + functionName);
                break;
        }
    }

}