package edu.upc.essi.dtim.NextiaCore.repositories;

public class LocalRepository extends DataRepository{
    String path;

    public LocalRepository() {
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
