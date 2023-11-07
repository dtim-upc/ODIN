package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMDuckDB;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMSpark;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DataLayerMaterialized;
import edu.upc.essi.dtim.NextiaJD.Discovery;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class jdModuleImpl implements jdModuleInterface {

    private final String dataLayerPath;
    public jdModuleImpl(@Autowired AppConfig appConfig) {
        this.dataLayerPath = appConfig.getDataLayerPath();
    }

    @Override
    public List<Alignment> getAlignments(Dataset dataset, Dataset dsB) throws SQLException, IOException, ClassNotFoundException {
        DataLayerMaterialized dlm = new DLMDuckDB(dataLayerPath);
        Discovery discovery = new Discovery(dlm);
        try {
            List<Alignment> alignmentsJD = discovery.getAlignments(dataset, dsB);
            return alignmentsJD;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
