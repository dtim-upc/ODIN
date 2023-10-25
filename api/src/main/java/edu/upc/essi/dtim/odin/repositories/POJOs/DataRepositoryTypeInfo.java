package edu.upc.essi.dtim.odin.repositories.POJOs;

public class DataRepositoryTypeInfo {
    private String label;
    private String value;

    public DataRepositoryTypeInfo(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public DataRepositoryTypeInfo() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

