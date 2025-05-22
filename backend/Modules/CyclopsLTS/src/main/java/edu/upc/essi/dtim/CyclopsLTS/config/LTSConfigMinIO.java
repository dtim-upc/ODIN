package edu.upc.essi.dtim.CyclopsLTS.config;

public class LTSConfigMinIO extends LTSConfig {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;

    public LTSConfigMinIO(String technology, String path, String endpoint, String accessKey, String secretKey, String bucket) {
        super(technology, path);
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public String getAccessKey() {
        return accessKey;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }

    @Override
    public String getBucket() {
        return bucket;
    }
}
