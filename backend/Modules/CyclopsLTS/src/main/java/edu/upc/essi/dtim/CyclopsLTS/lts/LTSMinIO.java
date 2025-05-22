package edu.upc.essi.dtim.CyclopsLTS.lts;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;

public class LTSMinIO extends LTS{

    private final MinioClient minioClient;
    private final String bucket;

    public LTSMinIO(String dataStorePath, String endpoint, String accessKey, String secretKey, String bucket){
        super(dataStorePath);
        this.bucket = bucket;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
    @Override
    public void uploadToLTS(Dataset d, String tableName) throws SQLException {
        // 1. Locate the Parquet file in the landing zone
        String parquetPath = Paths.get(dataStorePath, "landingZone", d.getUUID()).toString();
        File directoryPath = new File(parquetPath);
        String fileName = getParquetFile(directoryPath); // e.g. part-00000-*.parquet
        String localFilePath = Paths.get(parquetPath, fileName).toString();

        // 2. Upload to MinIO under formattedZone/
        System.out.println("Uploading " + localFilePath + " to MinIO bucket " + bucket + " at ltsZone/" + tableName + ".parquet");
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object("ltsZone/" + tableName + ".parquet") // gives it a clean name
                            .filename(localFilePath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload Parquet to MinIO", e);
        }
    }

    private String getParquetFile(File directoryPath) {
        String[] contents = directoryPath.list();
        if (contents == null || contents.length == 0) {
            throw new RuntimeException("Directory not found or empty: " + directoryPath);
        }
        for (String content : contents) {
            if (content.endsWith(".parquet")) {
                return content;
            }
        }
        throw new RuntimeException("No .parquet file found in: " + directoryPath);
    }


    @Override
    public void removeFromLTS(String tableName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object("ltsZone/" + tableName + ".parquet")
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove file from MinIO", e);
        }
    }

    @Override
    public void close() {

    }
}
