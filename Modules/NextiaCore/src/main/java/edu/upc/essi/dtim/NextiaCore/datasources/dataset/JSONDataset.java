package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

public class JSONDataset extends Dataset{
    private String path;

    public JSONDataset(){
        super();
    }

    public JSONDataset(String id, String name, String description, String path) {
        super(id, name, description);
        if (!path.endsWith(".json")) {
            throw new IllegalArgumentException("Invalid file format. Only JSON files are supported.");
        }
        else {
            this.path = path;
        }
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
