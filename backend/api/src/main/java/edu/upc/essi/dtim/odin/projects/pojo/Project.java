package edu.upc.essi.dtim.odin.projects.pojo;

import edu.upc.essi.dtim.NextiaCore.graph.MappingsGraph;
import edu.upc.essi.dtim.NextiaCore.repositories.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.queries.DataProduct;
import edu.upc.essi.dtim.NextiaCore.queries.Intent;

import java.util.List;

public class Project {
    private String projectId;
    private String projectName;
    private String projectDescription;
    private String projectPrivacy;
    private String projectColor;
    private String createdBy;
    private List<DataRepository> repositories;
    private IntegratedGraphJenaImpl integratedGraph;
    private MappingsGraph mappingsGraph;
    private IntegratedGraphJenaImpl temporalIntegratedGraph;
    private List<Dataset> integratedDatasets;
    private List<Dataset> temporalIntegratedDatasets;
    private List<DataProduct> dataProducts;
    private List<Intent> intents;

    public Project() {}

    public String getProjectId() {
        return projectId;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getProjectPrivacy() {
        return projectPrivacy;
    }
    public void setProjectPrivacy(String projectPrivacy) {
        this.projectPrivacy = projectPrivacy;
    }

    public String getProjectColor() {
        return projectColor;
    }
    public void setProjectColor(String projectColor) {
        this.projectColor = projectColor;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public IntegratedGraphJenaImpl getIntegratedGraph() {
        return integratedGraph;
    }
    public void setIntegratedGraph(IntegratedGraphJenaImpl integratedGraph) {
        this.integratedGraph = integratedGraph;
    }

    public MappingsGraph getMappingsGraph() {
        return mappingsGraph;
    }
    public void setMappingsGraph(MappingsGraph mappingsGraph) {
        this.mappingsGraph = mappingsGraph;
    }


    public IntegratedGraphJenaImpl getTemporalIntegratedGraph() {
        return temporalIntegratedGraph;
    }
    public void setTemporalIntegratedGraph(IntegratedGraphJenaImpl integratedGraph) {
        this.temporalIntegratedGraph = integratedGraph;
    }

    public List<DataRepository> getRepositories() {
        return repositories;
    }
    public void setRepositories(List<DataRepository> dataResources) {
        this.repositories = dataResources;
    }

    public List<Dataset> getIntegratedDatasets() {
        return integratedDatasets;
    }
    public void setIntegratedDatasets(List<Dataset> integratedDatasets) {
        this.integratedDatasets = integratedDatasets;
    }

    public List<Dataset> getTemporalIntegratedDatasets() {
        return this.temporalIntegratedDatasets;
    }
    public void setTemporalIntegratedDatasets(final List<Dataset> temporalIntegratedDatasets) {
        this.temporalIntegratedDatasets = temporalIntegratedDatasets;
    }

    public List<DataProduct> getDataProducts() { return dataProducts; }
    public void setDataProducts(List<DataProduct> dataProducts) { this.dataProducts = dataProducts; }
    public void addDataProduct(DataProduct dataProduct) { this.dataProducts.add(dataProduct); }

    public List<Intent> getIntents() { return intents; }
    public void setIntents(List<Intent> intents) { this.intents = intents;}
    public void addIntent(Intent intent) { this.intents.add(intent);}
}

