package edu.upc.essi.dtim.NextiaCore.datasets;

public class ParquetDataset extends Dataset{
    private String path;

    public ParquetDataset(){
        super();
    }

    public ParquetDataset(String id, String name, String description, String path) {
        super(id, name, description);
        if (!path.endsWith(".parquet")) {
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
