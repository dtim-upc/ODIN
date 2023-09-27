package edu.upc.essi.dtim.odin.repositories;

import java.util.ArrayList;
import java.util.List;

public class DataRepositoryTypeInfo {
    private String name;
    private String label;
    private List<FieldInfo> fields = new ArrayList<>();

    // Getters y setters para name y label

    public void addFieldInfo(String name, String label, String type) {
        FieldInfo fieldInfo = new FieldInfo(name, label, type);
        fields.add(fieldInfo);
    }

    public void addFieldInfo(FieldInfo fieldInfo) {
        fields.add(fieldInfo);
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

    public List<FieldInfo> getFields() {
        return fields;
    }

    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    // Getters y setters para fields
}

