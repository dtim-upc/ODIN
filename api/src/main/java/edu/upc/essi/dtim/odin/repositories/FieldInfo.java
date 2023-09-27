package edu.upc.essi.dtim.odin.repositories;

public class FieldInfo {
    private String name;
    private String label;
    private String type;

    public FieldInfo(String name, String label, String type) {
        this.name = name;
        this.label = label;
        this.type = type;
    }

    public FieldInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    // Constructores, getters y setters
}
