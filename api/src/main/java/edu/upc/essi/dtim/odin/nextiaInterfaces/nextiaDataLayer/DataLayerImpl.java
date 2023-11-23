package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.implementations.DataLayer;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLayerFactory;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;

public class DataLayerImpl implements DataLayerInterace {
    private final String dataLayerPath;
    private final String technology;

    public DataLayerImpl(@Autowired AppConfig appConfig) {
        this.dataLayerPath = appConfig.getDataLayerPath();
        this.technology = appConfig.getDataLayerTechnology();
    }

    private DataLayer getDataLayer() {
        DataLayer dl;
        try {
            dl = DataLayerFactory.getInstance(technology, dataLayerPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dl;
    }

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLoading dloading = DataLoadingSingleton.getInstance(dataLayerPath);
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

    @Override
    public String reconstructFile(MultipartFile multipartFile, String newFileDirectory) {
        DataLoading dataLoading = DataLoadingSingleton.getInstance(dataLayerPath);
        try {
            return dataLoading.reconstructFile(multipartFile.getInputStream(), newFileDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String reconstructTable(String tableName, String url, String username, String password) {
        DataLoading dataLoading = DataLoadingSingleton.getInstance(dataLayerPath);
        return dataLoading.reconstructTable(tableName, url, username, password);
    }
}
