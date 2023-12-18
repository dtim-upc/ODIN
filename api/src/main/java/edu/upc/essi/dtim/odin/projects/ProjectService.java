package edu.upc.essi.dtim.odin.projects;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ProjectService {
    ORMStoreInterface ormProject;
    private final AppConfig appConfig;

    /**
     * Constructs a new ProjectService.
     */
    public ProjectService(@Autowired AppConfig appConfig) {
        try {
            this.ormProject = ORMStoreFactory.getInstance();
            this.appConfig = appConfig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a dataset from the specified project.
     *
     * @param projectId The ID of the project to delete the dataset from.
     * @param datasetId The ID of the dataset to delete.
     * @throws IllegalArgumentException If the project with the given ID is not found.
     */
    public void deleteDatasetFromProject(String projectId, String datasetId) {
        // Retrieve the project with the given ID
        Project project = getProjectById(projectId);

        // Get the list of data repositories associated with the project
        List<DataRepository> repositoriesOfProject = project.getRepositories();
        boolean datasetFound = false;

        // Iterate through the data repositories using the iterator
        for (DataRepository repoInProject : repositoriesOfProject) {
            // Iterate through the datasets in each data repository
            Iterator<Dataset> datasetIterator = repoInProject.getDatasets().iterator();
            while (datasetIterator.hasNext()) {
                Dataset dataset = datasetIterator.next();

                // Check if the dataset ID matches the specified dataset ID
                if (datasetId.equals(dataset.getId())) {

                    // Remove the dataset from the data repository
                    datasetFound = true;
                    datasetIterator.remove();

                    // Save the updated list of data repositories and update the project's list
                    project.setRepositories(repositoriesOfProject);

                    break;
                }
            }
        }

        // Check if the dataset was not found in any data repository
        if (!datasetFound) {
            throw new IllegalArgumentException("Dataset not found");
        }

        // Save the updated project
        saveProject(project);
    }

    /**
     * Saves a project.
     *
     * @param project The project to save.
     * @return The saved project.
     */
    public Project saveProject(Project project) {
        Project savedProject = ormProject.save(project); // Save the project using the ORM store

        // Check if the project has an integrated or temporal integrated graph. If that is the case, set a name for them
        if (savedProject.getIntegratedGraph() != null && savedProject.getIntegratedGraph().getGraphName() != null) {
            try {
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
                Graph graph = project.getIntegratedGraph();
                // Set the graph name to match the saved project's integrated graph name
                graph.setGraphName(savedProject.getIntegratedGraph().getGraphName() == null ? "noName" : savedProject.getIntegratedGraph().getGraphName());
                graphStoreInterface.saveGraph(graph);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (savedProject.getTemporalIntegratedGraph() != null && savedProject.getTemporalIntegratedGraph().getGraphName() != null) {
            try {
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
                Graph graph = project.getTemporalIntegratedGraph();
                // Set the graph name to match the saved project's integrated graph name
                graph.setGraphName(savedProject.getTemporalIntegratedGraph().getGraphName() == null ? "noName" : savedProject.getTemporalIntegratedGraph().getGraphName());
                graphStoreInterface.saveGraph(graph);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return savedProject;
    }

    /**
     * Finds a project by its ID.
     *
     * @param projectId The ID of the project to find.
     * @return The found project, or null if not found.
     */
    public Project getProjectById(String projectId) {
        // Retrieve the project with the specified ID from the ORM store
        Project project = ormProject.findById(Project.class, projectId);
        if (project == null) {
            throw new IllegalArgumentException("Project not found with projectId: " + projectId);
        }

        // Check if the project has an integrated graph
        try {
            if (project.getIntegratedGraph() != null) {
                // Get the GraphStoreInterface from the GraphStoreFactory using the appConfig
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);

                // Retrieve the integrated graph by its graph name and cast it to IntegratedGraphJenaImpl
                Graph integratedGraph = graphStoreInterface.getGraph(project.getIntegratedGraph().getGraphName());
                project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
            }
            if (project.getTemporalIntegratedGraph() != null) {
                // Get the GraphStoreInterface from the GraphStoreFactory using the appConfig
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);

                // Retrieve the integrated graph by its graph name and cast it to IntegratedGraphJenaImpl
                Graph integratedGraph = graphStoreInterface.getGraph(project.getTemporalIntegratedGraph().getGraphName());
                project.setTemporalIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
            }
        } catch (Exception e) {
            // Throw a runtime exception if an error occurs while loading the integrated graph
            throw new RuntimeException(e);
        }

        // Return the found project (or null if not found)
        return project;
    }

    /**
     * Retrieves all projects.
     *
     * @return A list of all projects.
     */
    public List<Project> getAllProjects() {
        return ormProject.getAll(Project.class);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id The ID of the project to delete.
     * @return true if the project was deleted successfully, false otherwise.
     */
    public boolean deleteProject(String id) {
        // Before deleting the project from the ODIN database, we need to remove the datasets of the project from the Data layer
        Project p = ormProject.findById(Project.class, id);
        for (DataRepository dr: p.getRepositories()) {
            for (Dataset d: dr.getDatasets()) {
                deleteDatasetFromProject(id, d.getId());
            }
        }
        return ormProject.deleteOne(Project.class, id);
    }

    /**
     * Checks if a project contains a dataset with the given ID.
     *
     * @param projectId      The ID of the project to check.
     * @param dataresourceId The ID of the dataset to check.
     * @return true if the project contains the dataset, false otherwise.
     */
    public boolean projectContains(String projectId, String dataresourceId) {
        // Retrieve the project by its ID from the database
        Project project = getProjectById(projectId);

        // Get the list of repositories in the project
        List<DataRepository> repos = project.getRepositories();

        // Iterate through the repositories
        for (DataRepository repo : repos) {
            // Get the datasets associated with the current repository
            List<Dataset> datasets = repo.getDatasets();

            // Iterate through the datasets
            for (Dataset dataset : datasets) {
                // Get the ID of the current dataset
                String datasetId = dataset.getId();

                // Check if the dataset ID matches the provided dataresourceId
                if (datasetId.equals(dataresourceId)) {
                    // Dataset with the specified ID found in the project
                    return true; // Return true if the dataresourceId exists in any dataset
                }
            }
        }

        return false; // Return false if the dataresourceId is not found in any dataset
    }

    /**
     * Retrieves the datasets associated with a project.
     *
     * @param id The ID of the project.
     * @return A list of datasets belonging to the project.
     */
    public List<Dataset> getDatasetsOfProject(String id) {
        Project project = getProjectById(id);
        List<Dataset> datasets = new ArrayList<>();

        // Iterate through the repositories in the project and collect their datasets
        for (DataRepository repository : project.getRepositories()) {
            datasets.addAll(repository.getDatasets());
        }

        return datasets;
    }

    /**
     * Edits a project by updating its attributes if they have changed.
     *
     * @param project The modified project with updated attributes.
     * @return true if the project was edited and saved, false if no changes were detected.
     */
    public boolean editProject(Project project) {
        // Retrieve the original project from the database based on its ID
        Project originalProject = ormProject.findById(Project.class, project.getProjectId());

        // Check if any attribute has changed
        if (!project.getProjectName().equals(originalProject.getProjectName())
                || !project.getProjectDescription().equals(originalProject.getProjectDescription())
                || !project.getProjectColor().equals(originalProject.getProjectColor())
                || !project.getProjectPrivacy().equals(originalProject.getProjectPrivacy())
        ) {
            // At least one attribute has changed, update the original project
            originalProject.setProjectName(project.getProjectName());
            originalProject.setProjectDescription(project.getProjectDescription());
            originalProject.setProjectColor(project.getProjectColor());
            originalProject.setProjectPrivacy(project.getProjectPrivacy());

            // Perform the database update operation to save the changes
            saveProject(originalProject);

            return true; // Changes were detected and saved
        }

        // No changes detected, return false
        return false;
    }

    /**
     * Clones a project, creating a new project with the same structure and data as the original project.
     *
     * @param projectToClone The project to be cloned.
     * @return The cloned project.
     */
    public Project cloneProject(Project projectToClone) {
        // Reset the project ID to null to create a new project
        projectToClone.setProjectId(null);
        projectToClone.setProjectName(projectToClone.getProjectName() + " - Copy");

        // Get the list of repositories from the original project
        List<DataRepository> repositoriesToClone = projectToClone.getRepositories();

        if (repositoriesToClone != null && !repositoriesToClone.isEmpty()) {
            // Create a new list to store the cloned repositories
            List<DataRepository> clonedRepositories = new ArrayList<>();

            // Iterate through each repository in the original project
            for (DataRepository repositoryToClone : repositoriesToClone) {
                // Reset the repository ID to null to create a new repository
                repositoryToClone.setId(null);

                // Get the list of datasets from the original repository
                List<Dataset> datasetsToClone = repositoryToClone.getDatasets();

                if (datasetsToClone != null && !datasetsToClone.isEmpty()) {
                    // Create a new list to store the cloned datasets
                    List<Dataset> clonedDatasets = new ArrayList<>();

                    // Iterate through each dataset in the original repository
                    for (Dataset datasetToClone : datasetsToClone) {
                        // Reset the dataset ID to null to create a new dataset
                        datasetToClone.setId(null);

                        // Reset the graph name associated with the dataset to null
                        datasetToClone.getLocalGraph().setGraphName(null);

                        // Add the cloned dataset to the list of cloned datasets
                        clonedDatasets.add(datasetToClone);
                    }

                    // Set the list of cloned datasets to the cloned repository
                    repositoryToClone.setDatasets(clonedDatasets);
                }

                // Add the cloned repository to the list of cloned repositories
                clonedRepositories.add(repositoryToClone);
            }

            // Set the list of cloned repositories to the cloned project
            projectToClone.setRepositories(clonedRepositories);
        }

        // Reset the graph name associated with the integrated graph to null
        if (projectToClone.getIntegratedGraph() != null) {
            projectToClone.getIntegratedGraph().setGraphName(null);
        }

        // Save the cloned project and return it
        return saveProject(projectToClone);
    }

    /**
     * Adds a data repository to a project.
     *
     * @param projectId    The ID of the project to which the repository should be added.
     * @param repository   Repository to be added
     * @throws IllegalArgumentException If the project with the given ID is not found.
     */
    public void addRepositoryToProject(String projectId, DataRepository repository) {
        Project project = getProjectById(projectId);
        project.getRepositories().add(repository);
        saveProject(project);
    }

    /**
     * Retrieves the list of data repositories associated with a project.
     *
     * @param id The ID of the project.
     * @return A list of DataRepository objects belonging to the project.
     */
    public List<DataRepository> getRepositoriesOfProject(String id) {
        Project project = getProjectById(id);
        return project.getRepositories();
    }

    /**
     * Sets the dataset schema as the project schema.
     *
     * @param projectID The ID of the project.
     * @param datasetID The ID of the dataset whose schema should be set as the project schema.
     * @return true if the schema was set successfully, false otherwise.
     */
    public boolean setDatasetSchemaAsProjectSchema(String projectID, String datasetID) {
        Project project = getProjectById(projectID);
        Dataset dataset = ormProject.findById(Dataset.class, datasetID);

        if (project != null && dataset != null) {
            Graph integratedGraph = CoreGraphFactory.createIntegratedGraph();
            integratedGraph.setGraph(dataset.getLocalGraph().getGraph());

            // Set the dataset schema as the project schema
            project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);

            // Save the project to persist the changes
            saveProject(project);

            return true; // Schema set successfully
        }

        return false; // Project or dataset not found
    }

    public Project addIntegratedDataset(String projectID, String datasetID) {

        Project project = getProjectById(projectID);
        if (project != null) {
            // Check if the dataset has already been integrated in the project
            List<Dataset> integratedDatasets = project.getIntegratedDatasets();
            if (isDatasetIntegrated(integratedDatasets, datasetID)) {
                return project;
            } else {

                Dataset dataset = ormProject.findById(Dataset.class, datasetID);
                if (dataset != null) {
                    // Add the new dataset to the list of integrated datasets
                    integratedDatasets.add(dataset);

                    // Update the list of integrated datasets in the project
                    project.setIntegratedDatasets(integratedDatasets);

                    // Store the project to persist the changes
                    return saveProject(project);
                }
            }
        }
        return null; // Project/dataset not found
    }

    public Project addTemporalIntegratedDataset(String projectID, String datasetID) {

        Project project = getProjectById(projectID);
        if (project != null) {
            // Check if the dataset has already been integrated in the project
            List<Dataset> temporalIntegratedDatasets = project.getTemporalIntegratedDatasets();
            if (isDatasetIntegrated(temporalIntegratedDatasets, datasetID)) {
                return project;
            } else {

                Dataset dataset = ormProject.findById(Dataset.class, datasetID);
                if (dataset != null) {
                    // Add the new dataset to the list of integrated datasets
                    temporalIntegratedDatasets.add(dataset);

                    // Update the list of integrated datasets in the project
                    project.setTemporalIntegratedDatasets(temporalIntegratedDatasets);

                    // Store the project to persist the changes
                    return saveProject(project);
                }
            }
        }
        return null; // Project/dataset not found
    }

    // Auxiliary function to check if a dataset has already been integrated
    private boolean isDatasetIntegrated(List<Dataset> integratedDatasets, String datasetID) {
        for (Dataset integratedDataset : integratedDatasets) {
            if (integratedDataset.getId().equals(datasetID)) {
                return true;
            }
        }
        return false;
    }

    public void deleteIntegratedDatasets(String projectID) {
        Project project = getProjectById(projectID);
        project.setIntegratedDatasets(new ArrayList<>());
        saveProject(project);
    }
}

