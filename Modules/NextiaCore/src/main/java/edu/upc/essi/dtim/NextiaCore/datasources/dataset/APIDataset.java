package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

public class APIDataset extends Dataset{
    String endpoint;
    String jsonPath; // Path were a temporary JSON file with the data from the API will be stored to be processed more easily

    public APIDataset(){
        super();
    }

    public APIDataset(String id, String name, String description, String endpoint, String jsonPath) {
        super(id, name, description);
        this.endpoint = endpoint;
        this.jsonPath = jsonPath;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
