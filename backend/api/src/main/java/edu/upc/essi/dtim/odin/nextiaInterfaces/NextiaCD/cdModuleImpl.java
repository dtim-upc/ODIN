package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaCD;

import edu.upc.essi.dtim.nextiacd.ConstraintDiscovery;
import edu.upc.essi.dtim.nextiacd.IConstraintDiscovery;
import edu.upc.essi.dtim.NextiaCore.constraints.DenialConstraint;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerSingleton;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class cdModuleImpl implements cdModuleInterface {
    @Autowired
    private static AppConfig appConfig;

    @Override
    public List<DenialConstraint> getDCs(Dataset dataset) {
        // NextiaCD needs access to the data and, as such, the data layer, so we need to pass it as a parameter
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        String dqServiceUrl = appConfig.getDqServiceUrl();
        IConstraintDiscovery Cdiscovery = new ConstraintDiscovery(dl, dqServiceUrl);
        try {
            return Cdiscovery.getDCs(dataset);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("There was an error when computing the alignments", e.getMessage());
        }
    }

    private String getParquetFile(File directoryPath) {
        String[] contents = directoryPath.list();
        if (contents == null || contents.length == 0) {
            throw new RuntimeException("Directory not found or empty: " + directoryPath);
        }
        for (String content : contents) {
            if (content.endsWith(".parquet")) {
                return content;
            }
        }
        throw new RuntimeException("No .parquet file found in: " + directoryPath);
    }


    @Override
    public String getIdentifier(Dataset dataset) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        String dqServiceUrl = appConfig.getDqServiceUrl();
        IConstraintDiscovery Cdiscovery = new ConstraintDiscovery(dl, dqServiceUrl);
        try {
            return Cdiscovery.getIdentifier(dataset);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("There was an error when computing the alignments", e.getMessage());
        }
    }
}
