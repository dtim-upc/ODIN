package edu.upc.essi.dtim.odin.projects;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;

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
    private IntegratedGraphJenaImpl temporalIntegratedGraph;
    private List<Dataset> integratedDatasets;
    private List<Dataset> temporalIntegratedDatasets;

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

    public void setTemporalIntegratedGraph(IntegratedGraphJenaImpl integratedGraph) {
        this.temporalIntegratedGraph = integratedGraph;
    }

    public IntegratedGraphJenaImpl getTemporalIntegratedGraph() {
        return temporalIntegratedGraph;
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
}

