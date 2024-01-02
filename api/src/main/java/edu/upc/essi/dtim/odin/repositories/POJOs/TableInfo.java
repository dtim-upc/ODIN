package edu.upc.essi.dtim.odin.repositories.POJOs;

public class TableInfo {
    private String name;
    private String size;
    private String otherInfo;

    public TableInfo() {}

    public TableInfo(String name, String size, String otherInfo) {
        this.name = name;
        this.size = size;
        this.otherInfo = otherInfo;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    public String getOtherInfo() {
        return otherInfo;
    }
    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }
}

