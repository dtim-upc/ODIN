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

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLoading dloading = DataLoadingSingleton.getInstance(dataLayerPath);

        System.out.println(dataset.getWrapper());
        dloading.uploadToLandingZone(dataset);

        DataLayer dl = null;
        try {
            dl = DataLayerFactory.getInstance(technology,dataLayerPath);
            dl.uploadToFormattedZone(dataset, dataset.getDataLayerPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDataset(String dataLayerPath) {
        //todo
        DataLoading dl = DataLoadingSingleton.getInstance(dataLayerPath);

        //dl.deleteFromLandingZone(dataLayerPath);
    }
}
