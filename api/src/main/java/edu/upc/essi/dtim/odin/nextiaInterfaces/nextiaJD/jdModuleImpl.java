package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaDataLayer.implementations.DataLayer;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.NextiaJD.Discovery;
import edu.upc.essi.dtim.NextiaJD.IDiscovery;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerSingleton;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class jdModuleImpl implements jdModuleInterface {

    private static AppConfig appConfig;

    public jdModuleImpl(@Autowired AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public List<Alignment> getAlignments(Dataset dataset, Dataset dsB) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        IDiscovery discovery = new Discovery(dl);
        System.out.println(dataset.getDataLayerPath());
        System.out.println(dsB.getDataLayerPath());
        try {
            return discovery.getAlignments(dataset, dsB);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
