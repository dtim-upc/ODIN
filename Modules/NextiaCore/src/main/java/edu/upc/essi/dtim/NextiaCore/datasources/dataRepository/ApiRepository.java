package edu.upc.essi.dtim.NextiaCore.datasources.dataRepository;

public class ApiRepository extends DataRepository{
    public ApiRepository() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String url;
}