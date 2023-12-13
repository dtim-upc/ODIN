package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;

public class DataLayerImpl implements DataLayerInterface {
    private static AppConfig appConfig;

    public DataLayerImpl(@Autowired AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public boolean uploadToDataLayer(Dataset dataset) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        try {
            dl.uploadToLandingZone(dataset);
            dl.uploadToFormattedZone(dataset, dataset.getUUID());
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void deleteDataset(String UUID) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        try {
            dl.removeFromLandingZone(UUID);
            dl.removeFromFormattedZone(UUID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String storeTemporalFile(MultipartFile multipartFile, String newFileDirectory) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        try {
            return dl.storeTemporalFile(multipartFile.getInputStream(), newFileDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
