package edu.upc.essi.dtim.nextiabs;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;

public class BootstrapFactory {
    private static NextiaBootstrapInterface instance = null;

    private BootstrapFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    public static NextiaBootstrapInterface getInstance(Dataset dataset) throws Exception {
        if (dataset instanceof CsvDataset) {
            instance = new CSVBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset instanceof JsonDataset || dataset instanceof APIDataset) {
            instance = new JSONBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset instanceof SQLDataset) {
            instance = new SQLBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset instanceof XmlDataset) {
            instance = new XMLBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset instanceof ParquetDataset) {
            instance = new ParquetBootstrap_with_DataFrame_MM_without_Jena();
        } else {
            throw new IllegalArgumentException("Unsupported dataset type");
        }
        return instance;
    }
}
