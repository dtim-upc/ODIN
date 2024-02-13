package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;

import java.util.List;

/**
 * A POJO (Plain Old Java Object) class representing integration data.
 */
public class IntegrationData {
    private Dataset dsA; // The first dataset to be integrated
    private Dataset dsB; // The second dataset to be integrated.
    private String integratedName; // Name of the integrated data
    private List<Alignment> alignments;

    public Dataset getDsA() {
        return dsA;
    }
    public void setDsA(Dataset dsA) {
        this.dsA = dsA;
    }

    public Dataset getDsB() {
        return dsB;
    }
    public void setDsB(Dataset dsB) {
        this.dsB = dsB;
    }

    public String getIntegratedName() {
        return integratedName;
    }
    public void setIntegratedName(String integratedName) {
        this.integratedName = integratedName;
    }

    public List<Alignment> getAlignments() {
        return alignments;
    }
    public void setAlignments(List<Alignment> alignments) {
        this.alignments = alignments;
    }
}
