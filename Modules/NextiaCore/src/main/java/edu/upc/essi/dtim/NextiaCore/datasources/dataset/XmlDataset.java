package edu.upc.essi.dtim.NextiaCore.datasources.dataset;

public class XmlDataset extends Dataset{
    public XmlDataset(){
        super();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    /**
     * Constructor for the JsonDataset class.
     *
     * @param id          The ID of the dataset.
     * @param name        The name of the dataset.
     * @param description A description of the dataset.
     * @param path        The path to the dataset file.
     */
    public XmlDataset(String id, String name, String description, String path) {
        super(id, name, description);
        if (!path.endsWith(".xml")) {
            throw new IllegalArgumentException("Invalid file format. Only JSON files are supported.");
        }
        this.path = path;
    }
}
