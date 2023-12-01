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
    private static AppConfig appConfig;

    public DataLayerImpl(@Autowired AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        DataLoading dLoading = dl.getDataLoading();
        dLoading.uploadToLandingZone(dataset);

        try {
            dl.uploadToFormattedZone(dataset, dataset.getDataLayerPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void deleteDataset(String dataLayerPath) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        try {
            dl.RemoveFromFormattedZone(dataLayerPath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String reconstructFile(MultipartFile multipartFile, String newFileDirectory) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        DataLoading dLoading = dl.getDataLoading();
        try {
            return dLoading.reconstructFile(multipartFile.getInputStream(), newFileDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
