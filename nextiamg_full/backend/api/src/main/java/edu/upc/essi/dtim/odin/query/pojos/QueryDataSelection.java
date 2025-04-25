package edu.upc.essi.dtim.odin.query.pojos;

import java.util.List;

public class QueryDataSelection {

//    String graphIRI;
    String graphID;
    String graphType;
    List<Classes> classes; // It contains IRIs
    List<Property> properties;

    public String getGraphID() {
        return graphID;
    }

    public void setGraphID(String graphID) {
        this.graphID = graphID;
    }

    public String getGraphType() {
        return graphType;
    }

    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }

    public List<Classes> getClasses() {
        return classes;
    }

    public void setClasses(List<Classes> classes) {
        this.classes = classes;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }
}
