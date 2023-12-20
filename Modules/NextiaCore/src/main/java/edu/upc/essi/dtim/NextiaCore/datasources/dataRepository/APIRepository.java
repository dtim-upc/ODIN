package edu.upc.essi.dtim.NextiaCore.datasources.dataRepository;

public class APIRepository extends DataRepository{
    String url;

    public APIRepository() {}

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}