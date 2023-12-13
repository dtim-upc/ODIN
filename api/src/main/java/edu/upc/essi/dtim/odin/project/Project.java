package edu.upc.essi.dtim.odin.project;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;

import java.util.List;

/**
 * Represents a project in the system.
 */
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

    public List<Dataset> getTemporalIntegratedDatasets() {
        return this.temporalIntegratedDatasets;
    }

    public void setTemporalIntegratedDatasets(final List<Dataset> temporalIntegratedDatasets) {
        this.temporalIntegratedDatasets = temporalIntegratedDatasets;
    }

    private List<Dataset> temporalIntegratedDatasets;

    /**
     * Get the unique identifier of the project.
     *
     * @return The project's unique identifier.
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Set the unique identifier of the project.
     *
     * @param projectId The project's unique identifier.
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * Get the name of the project.
     *
     * @return The project's name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the name of the project.
     *
     * @param projectName The project's name.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Get the description of the project.
     *
     * @return The project's description.
     */
    public String getProjectDescription() {
        return projectDescription;
    }

    /**
     * Set the description of the project.
     *
     * @param projectDescription The project's description.
     */
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    /**
     * Get the privacy setting of the project.
     *
     * @return The project's privacy setting.
     */
    public String getProjectPrivacy() {
        return projectPrivacy;
    }

    /**
     * Set the privacy setting of the project.
     *
     * @param projectPrivacy The project's privacy setting.
     */
    public void setProjectPrivacy(String projectPrivacy) {
        this.projectPrivacy = projectPrivacy;
    }

    /**
     * Get the color associated with the project.
     *
     * @return The project's color.
     */
    public String getProjectColor() {
        return projectColor;
    }

    /**
     * Set the color associated with the project.
     *
     * @param projectColor The project's color.
     */
    public void setProjectColor(String projectColor) {
        this.projectColor = projectColor;
    }

    /**
     * Get the username of the user who created the project.
     *
     * @return The username of the project creator.
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the username of the user who created the project.
     *
     * @param createdBy The username of the project creator.
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Get the integrated graph associated with the project.
     *
     * @return The integrated graph.
     */
    public IntegratedGraphJenaImpl getIntegratedGraph() {
        return integratedGraph;
    }

    /**
     * Set the integrated graph associated with the project.
     *
     * @param integratedGraph The integrated graph.
     */
    public void setIntegratedGraph(IntegratedGraphJenaImpl integratedGraph) {
        this.integratedGraph = integratedGraph;
    }

    /**
     * Set the integrated graph associated with the project.
     *
     * @param integratedGraph The integrated graph.
     */
    public void setTemporalIntegratedGraph(IntegratedGraphJenaImpl integratedGraph) {
        this.temporalIntegratedGraph = integratedGraph;
    }

    /**
     * Get the integrated graph associated with the project.
     *
     * @return The integrated graph.
     */
    public IntegratedGraphJenaImpl getTemporalIntegratedGraph() {
        return temporalIntegratedGraph;
    }

    /**
     * Get the list of data repositories associated with the project.
     *
     * @return The list of data repositories.
     */
    public List<DataRepository> getRepositories() {
        return repositories;
    }

    /**
     * Set the list of data repositories associated with the project.
     *
     * @param dataResources The list of data repositories.
     */
    public void setRepositories(List<DataRepository> dataResources) {
        this.repositories = dataResources;
    }

    public List<Dataset> getIntegratedDatasets() {
        return integratedDatasets;
    }

    public void setIntegratedDatasets(List<Dataset> integratedDatasets) {
        this.integratedDatasets = integratedDatasets;
    }
}

