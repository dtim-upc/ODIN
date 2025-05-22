package edu.upc.essi.dtim.CyclopsLTS.config;

public abstract class LTSConfig {
    private String technology;
    private String path;

    public LTSConfig(String technology, String path) {
        this.technology = technology;
        this.path = path;
    }

    public String getTechnology() {
        return technology;
    }

    public String getPath() {
        return path;
    }

    // Optional MinIO methods
    public String getEndpoint() {
        return null;
    };
    public String getAccessKey(){
        return null;
    };
    public String getSecretKey(){
        return null;
    };
    public String getBucket(){
        return null;
    };
}
