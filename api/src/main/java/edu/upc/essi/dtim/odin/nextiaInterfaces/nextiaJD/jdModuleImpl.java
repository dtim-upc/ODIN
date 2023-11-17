package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaDataLayer.implementations.DataLayer;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.NextiaJD.Discovery;
import edu.upc.essi.dtim.NextiaJD.IDiscovery;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class jdModuleImpl implements jdModuleInterface {

    private final String dataLayerPath;
    private final String technology;

    public jdModuleImpl(@Autowired AppConfig appConfig) {
        this.dataLayerPath = appConfig.getDataLayerPath();
        this.technology = appConfig.getDataLayerTechnology();
    }

    @Override
    public List<Alignment> getAlignments(Dataset dataset, Dataset dsB) {
        DataLayer dl = null;
        try {
            dl = DataLayerFactory.getInstance(technology, dataLayerPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        IDiscovery discovery = new Discovery(dl);
        try {
            List<Alignment> alignmentsJD = discovery.getAlignments(dataset, dsB);
            return alignmentsJD;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
