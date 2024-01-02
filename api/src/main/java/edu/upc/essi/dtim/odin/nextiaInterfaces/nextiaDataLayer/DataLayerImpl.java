package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.CustomIOException;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class DataLayerImpl implements DataLayerInterface {
    private static AppConfig appConfig;

    public DataLayerImpl(@Autowired AppConfig appConfig) {
        DataLayerImpl.appConfig = appConfig;
    }

    @Override
    public void uploadToDataLayer(Dataset dataset) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        try {
            dl.uploadToLandingZone(dataset);
            dl.uploadToFormattedZone(dataset, dataset.getUUID());
        }
        catch (Exception e) {
            throw new InternalServerErrorException("Error when uploading the data to the data layer", e.getMessage());
        }
    }

    @Override
    public void deleteDataset(String UUID) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        try {
            dl.removeFromLandingZone(UUID);
            dl.removeFromFormattedZone(UUID);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error when deleting the dataset from the data layer", e.getMessage());
        }
    }

    @Override
    public String storeTemporalFile(MultipartFile multipartFile, String newFileDirectory) {
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        try {
            return dl.storeTemporalFile(multipartFile.getInputStream(), newFileDirectory);
        } catch (IOException e) {
            throw new CustomIOException("Error when storing the temporal file");
        }
    }
}
