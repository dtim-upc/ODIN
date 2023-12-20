package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

public class XMLDataset extends Dataset{
    private String path;
    public XMLDataset(){
        super();
    }

    public XMLDataset(String id, String name, String description, String path) {
        super(id, name, description);
        if (!path.endsWith(".xml")) {
            throw new IllegalArgumentException("Invalid file format. Only JSON files are supported.");
        }
        this.path = path;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
