package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaCD;

import edu.upc.essi.dtim.NextiaCD.ConstraintDiscovery;
import edu.upc.essi.dtim.NextiaCD.IConstraintDiscovery;
import edu.upc.essi.dtim.NextiaCore.constraints.DenialConstraint;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.NextiaJD.discovery.Discovery;
import edu.upc.essi.dtim.NextiaJD.discovery.IDiscovery;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerSingleton;
import org.springframework.beans.factory.annotation.Autowired;

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
}
