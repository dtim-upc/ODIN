package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMDuckDB;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMSpark;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DataLayerMaterialized;
import edu.upc.essi.dtim.NextiaJD.Discovery;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class jdModuleImpl implements jdModuleInterface {
    @Override
    public List<Alignment> getAlignments(Dataset dataset, Dataset dsB) throws SQLException, IOException, ClassNotFoundException {
        String datalayerPath = "C:\\temp";
        DataLayerMaterialized dlm = new DLMDuckDB(datalayerPath);
        Discovery discovery = new Discovery(dlm);
        try {
            List<Alignment> alignmentsJD = discovery.getAlignments(dataset, dsB);
            return alignmentsJD;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
