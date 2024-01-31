package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.NextiaJD.discovery.Discovery;
import edu.upc.essi.dtim.NextiaJD.discovery.IDiscovery;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerSingleton;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class jdModuleImpl implements jdModuleInterface {
    @Autowired
    private static AppConfig appConfig;

    @Override
    public List<Alignment> getAlignments(Dataset dataset, Dataset dataset2) {
        // NextiaJD needs access to the data and, as such, the data layer, so we need to pass it as a parameter
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        IDiscovery discovery = new Discovery(dl);
        try {
            return discovery.getAlignments(dataset, dataset2);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("There was an error when computing the alignments", e.getMessage());
        }
    }
}
