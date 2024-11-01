package edu.upc.essi.dtim.nextiabs.bootstrap;

import edu.upc.essi.dtim.NextiaCore.datasets.*;
import edu.upc.essi.dtim.NextiaCore.repositories.RelationalJDBCRepository;
import edu.upc.essi.dtim.nextiabs.databaseConnection.PostgresSQLImpl;
import edu.upc.essi.dtim.nextiabs.implementations.*;

public class BootstrapFactory {
    private static BootstrapODIN instance = null;

    private BootstrapFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    public static BootstrapODIN getInstance(Dataset dataset) throws Exception {
        if (dataset instanceof CSVDataset) {
            instance = new CSVBootstrap(dataset.getId(), dataset.getDatasetName(), ((CSVDataset) dataset).getPath());
        } else if (dataset instanceof JSONDataset) {
            instance = new JSONBootstrap(dataset.getId(), dataset.getDatasetName(), ((JSONDataset) dataset).getPath());
        } else if (dataset instanceof APIDataset) {
            instance = new JSONBootstrap(dataset.getId(), dataset.getDatasetName(), ((APIDataset) dataset).getJsonPath());
        } else if (dataset instanceof SQLDataset) {
            String url = ((RelationalJDBCRepository) dataset.getRepository()).getUrl();
            String databaseName = url.substring(url.lastIndexOf("/") + 1);
            instance = new SQLBootstrap(dataset.getId(), dataset.getDatasetName(), ((SQLDataset) dataset).getTableName(), new PostgresSQLImpl(),
                    ((SQLDataset) dataset).getHostname(), ((SQLDataset) dataset).getPort(), ((SQLDataset) dataset).getUsername(),
                    ((SQLDataset) dataset).getPassword(), databaseName);
        } else if (dataset instanceof XMLDataset) {
            instance = new XMLBootstrap(dataset.getId(), dataset.getDatasetName(), ((XMLDataset) dataset).getPath());
        } else if (dataset instanceof ParquetDataset) {
            instance = new ParquetBootstrap(dataset.getId(), dataset.getDatasetName(), ((ParquetDataset) dataset).getPath());
        } else {
            throw new IllegalArgumentException("Unsupported dataset type");
        }
        return instance;
    }
}
