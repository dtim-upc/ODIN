package edu.upc.essi.dtim.NextiaCore.mappings;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;

public class Mappings {
    private String mappingType;
    private Graph graphI;



    private String UUID;

    public Mappings(String mappingType, Graph graphI) {
        this.mappingType = mappingType;
        this.graphI = graphI;
    }
    public String getMappingType() {
        return mappingType;
    }
    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }
    public Graph getGraphI() {
        return graphI;
    }
    public void setGraphI(Graph graphI) {
        this.graphI = graphI;
    }

    public String getUUID() {
        return UUID;
    }
    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
