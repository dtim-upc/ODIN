package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import org.springframework.web.multipart.MultipartFile;

public interface DataLayerInterface {
    /**
     * @param dataset The dataset to upload to DataLayer
     */
    boolean uploadToDataLayer(Dataset dataset);

    void deleteDataset(String UUID);

    String storeTemporalFile(MultipartFile multipartFile, String repositoryIdAndName);
}
