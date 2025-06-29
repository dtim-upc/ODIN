package edu.upc.essi.dtim.NextiaDataLayer.dataCollectors;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.repositories.APIRepository;
import edu.upc.essi.dtim.NextiaCore.datasets.APIDataset;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer.storeTemporalFile;

public class DataCollectorAPI extends DataCollector {
    public DataCollectorAPI(String dataStorePath) {
        super(dataStorePath);
    }

    private final boolean demoMode = Boolean.parseBoolean(System.getenv().getOrDefault("DEMO_MODE", "false"));


    @Override
    public void uploadDataToTemporalFolder(Dataset d) {
        try {
            APIRepository repo = (APIRepository) d.getRepository();
            APIDataset ad = (APIDataset) d;
            if (demoMode) {

                URL parsedUrl = new URL(ad.getEndpoint());

                // Endpoint (scheme + host + port)
                String endpoint = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
                if (parsedUrl.getPort() != -1) {
                    endpoint += ":" + parsedUrl.getPort();
                }

                // Path segments
                String[] pathSegments = parsedUrl.getPath().substring(1).split("/", 2); // Remove leading `/` and split

                if (pathSegments.length < 2) {
                    throw new IllegalArgumentException("URL must include both bucket and object key: " + ad.getEndpoint());
                }

                String bucket = pathSegments[0];        // "long-term-storage"
                String objectKey = pathSegments[1];     // "MDS_part1.csv"

                MinioClient minioClient = MinioClient.builder()
                        .endpoint(endpoint)
                        .credentials("minioadmin", "minioadmin123")
                        .build();

                try (InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectKey)
                                .build()
                )) {
                    String jsonPath = storeTemporalFile(dataStorePath, stream, d.getUUID() + ".json");
                    ad.setJsonPath(jsonPath);
                } catch (ServerException e) {
                    throw new RuntimeException(e);
                } catch (InsufficientDataException e) {
                    throw new RuntimeException(e);
                } catch (ErrorResponseException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                } catch (InvalidResponseException e) {
                    throw new RuntimeException(e);
                } catch (XmlParserException e) {
                    throw new RuntimeException(e);
                } catch (InternalException e) {
                    throw new RuntimeException(e);
                }
            } else {

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
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
