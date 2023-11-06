package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMSpark;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DataLayerMaterialized;
import edu.upc.essi.dtim.NextiaJD.Discovery;

import java.util.List;

public class jdModuleImpl implements jdModuleInterface {
    @Override
    public List<Alignment> getAlignments(Dataset dataset, Dataset dsB) {
        String datalayerPath = "C:\\Users\\victor.asenjo\\Documents\\GitHub\\ODIN\\api\\dbFiles\\dataLayer";
        DataLayerMaterialized dlm = new DLMSpark(datalayerPath);
        Discovery discovery = new Discovery(dlm);
        try {
            List<Alignment> alignmentsJD = discovery.getAlignments(dataset, dsB);
            return alignmentsJD;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
