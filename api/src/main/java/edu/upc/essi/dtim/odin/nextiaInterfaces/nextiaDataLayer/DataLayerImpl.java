package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.implementations.DataLayer;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

public class DataLayerImpl implements DataLayerInterace{
    private final String dataLayerPath;
    private final String technology;

    public DataLayerImpl(@Autowired AppConfig appConfig) {
        this.dataLayerPath = appConfig.getDataLayerPath();
        this.technology = appConfig.getDataLayerTechnology();
    }

    private DataLayer getDataLayer() {
        DataLayer dl;
        try {
            dl = DataLayerFactory.getInstance(technology,dataLayerPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dl;
    }

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLoading dloading = DataLoadingSingleton.getInstance(dataLayerPath);

        System.out.println(dataset.getWrapper());
        dloading.uploadToLandingZone(dataset);

        DataLayer dl = getDataLayer();
        try {
            dl.uploadToFormattedZone(dataset, dataset.getDataLayerPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDataset(String dataLayerPath) {
        DataLayer dl = getDataLayer();
        try {
            dl.RemoveFromFormattedZone(dataLayerPath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
