package edu.upc.essi.dtim.odin.repositories.POJOs;

public class DataRepositoryTypeInfo {
    private String name;
    private String value;

    public DataRepositoryTypeInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public DataRepositoryTypeInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

