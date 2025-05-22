package edu.upc.essi.dtim.odin.nextiaInterfaces.cyclopsLTS;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;


public interface LTSInterface {

    /**
     * Uploads a dataset to the LTS, in a permanent folder/database to be accessed afterward.
     *
     * @param dataset The dataset to be uploaded.
     */
    void uploadToLTS(Dataset dataset);

    /**
     * Removes a dataset from the LTS
     *
     * @param UUID The identifier of the dataset to be removed
     */
    void deleteDatasetFromLTS(String UUID);
}
