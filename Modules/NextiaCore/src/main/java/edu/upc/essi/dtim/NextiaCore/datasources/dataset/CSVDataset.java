package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

public class CSVDataset extends Dataset{
    private String path;

    public CSVDataset(){
        super();
    }
    public CSVDataset(String id, String name, String description, String path) {
        super(id, name, description);
        if (!path.endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file format. Only CSV files are supported.");
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
