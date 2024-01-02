package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import org.springframework.web.multipart.MultipartFile;

public interface DataLayerInterface {
    /**
     * Uploads a dataset to the data layer, in a permanent folder/database to be accessed afterward.
     *
     * @param dataset The dataset to be uploaded.
     */
    void uploadToDataLayer(Dataset dataset);

    /**
     * Removes a dataset from the data layer
     *
     * @param UUID The identifier of the dataset to be removed
     */
    void deleteDataset(String UUID);

    /**
     * Stores a file in the data layer. This storage will be temporary and will eventually be deleted, which is required
     * in some processes (e.g. when getting data from an API we need to store first a temporal json to get/apply the
     * wrapper).
     *
     * @param multipartFile       Data to be uploaded
     * @param repositoryIdAndName Path to where the data will be stored
     * @return A boolean indicating whether the file was uploaded successfully.
     */
    String storeTemporalFile(MultipartFile multipartFile, String repositoryIdAndName);
}
