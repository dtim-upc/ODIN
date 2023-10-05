package edu.upc.essi.dtim.odin.repositories;

public class TableInfo {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;  // Nombre de la tabla

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

    private String size;    // Tamaño de la tabla (puedes cambiar el tipo de dato según tus necesidades)
    private String otherInfo;  // Otra información relevante

    // Constructores, getters y setters

    // Constructor principal
    public TableInfo(String name, String size, String otherInfo) {
        this.name = name;
        this.size = size;
        this.otherInfo = otherInfo;
    }

    public TableInfo() {
    }

}

