package edu.upc.essi.dtim.nextiabs.utils;

public class JSON_Aux {
    String key;
    String label;
    String path;

    public JSON_Aux(String key, String label, String path) {
        this.key = key;
        this.label = label;
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
