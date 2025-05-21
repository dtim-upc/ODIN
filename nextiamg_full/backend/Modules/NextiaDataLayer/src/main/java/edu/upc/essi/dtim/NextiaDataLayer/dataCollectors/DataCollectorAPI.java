package edu.upc.essi.dtim.NextiaDataLayer.dataCollectors;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.repositories.APIRepository;
import edu.upc.essi.dtim.NextiaCore.datasets.APIDataset;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer.storeTemporalFile;

public class DataCollectorAPI extends DataCollector {
    public DataCollectorAPI(String dataStorePath) {
        super(dataStorePath);
    }

    @Override
    public void uploadDataToTemporalFolder(Dataset d) {
        try {
            APIRepository repo = (APIRepository) d.getRepository();
            APIDataset ad = (APIDataset) d;
            // Connection parameters
            URL url = new URL(repo.getUrl() + ad.getEndpoint());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) { // Success
                // Read the data from the API (json format) and store it in \tmp (json file)
                String jsonPath = storeTemporalFile(dataStorePath, con.getInputStream(), d.getUUID() + ".json");
                ((APIDataset) d).setJsonPath(jsonPath); // add path of the json file to be accessed later

            } else {
                throw new RuntimeException("API call did not work");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
