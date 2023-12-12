package edu.upc.essi.dtim.NextiaDataLayer.utils;

import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DLDuckDB;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DLSpark;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;

public class DataLayerFactory {
    private static DataLayer instance = null;

    private DataLayerFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    public static DataLayer getInstance(String technology, String dataLayerPath) throws Exception {
        switch (technology) {
            case "DuckDB":
                instance = new DLDuckDB(dataLayerPath);
                break;
            case "Spark":
                instance = new DLSpark(dataLayerPath);
                break;
            default:
                instance = new DLDuckDB(dataLayerPath);
        }
        return instance;
    }
}

