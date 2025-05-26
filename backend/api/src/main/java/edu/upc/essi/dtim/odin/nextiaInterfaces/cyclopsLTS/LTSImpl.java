package edu.upc.essi.dtim.odin.nextiaInterfaces.cyclopsLTS;

import edu.upc.essi.dtim.CyclopsLTS.lts.LTS;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerSingleton;
import org.springframework.beans.factory.annotation.Autowired;

public class LTSImpl implements LTSInterface {
    private static AppConfig appConfig;

    public LTSImpl(@Autowired AppConfig appConfig) {
        LTSImpl.appConfig = appConfig;
    }

    @Override
    public void uploadToLTS(Dataset dataset) {
        LTS lts = LTSSingleton.getInstance(appConfig);
        try {
            lts.uploadToLTS(dataset, dataset.getDatasetName()+"_"+dataset.getId());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Error when uploading the data to the LTS", e.getMessage());
        }
    }

    @Override
    public void deleteDatasetFromLTS(String UUID) {
        LTS lts = LTSSingleton.getInstance(appConfig);
        try {
            lts.removeFromLTS(UUID);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error when deleting the dataset from the LTS", e.getMessage());
        }
    }

}
