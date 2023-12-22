package edu.upc.essi.dtim.odin.projects;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.NoChangesException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
     * Saves a project.
     *
     * @param project The project to save.
     * @return The saved project.
     */
    public Project saveProject(Project project) {
        // For some reason, ormProject.save(project) does not store the global graph of the integrated graphs, so we need to
        // add them manually (last line of each if). This is not a problem in the getProject function, as the global
        // graphs are calculated anew in the function.
        Project savedProject = ormProject.save(project); // Save the project using the ORM store

        // Check if the project has an integrated or temporal integrated graph. If that is the case, set a name for them and store them
        if (savedProject.getIntegratedGraph() != null && savedProject.getIntegratedGraph().getGraphName() != null) {
            try {
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
                Graph graph = project.getIntegratedGraph();
                // Set the graph name to match the saved project's integrated graph name
                graph.setGraphName(savedProject.getIntegratedGraph().getGraphName() == null ? "noName" : savedProject.getIntegratedGraph().getGraphName());
                graphStoreInterface.saveGraph(graph);
                savedProject.getIntegratedGraph().setGlobalGraph(project.getIntegratedGraph().getGlobalGraph());
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
                savedProject.getTemporalIntegratedGraph().setGlobalGraph(project.getTemporalIntegratedGraph().getGlobalGraph());
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
    public Project getProject(String projectId) {
        // Retrieve the project with the specified ID from the ORM store
        Project project = ormProject.findById(Project.class, projectId);
        if (project == null) {
            throw new NoSuchElementException("Project not found with ID: " + projectId);
        }

        // Check if the project has an integrated graph or a temporal integrated graph. If so, add it
        try {
            if (project.getIntegratedGraph() != null) {
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);

                // Retrieve the integrated graph by its graph name and cast it to IntegratedGraphJenaImpl
                Graph integratedGraph = graphStoreInterface.getGraph(project.getIntegratedGraph().getGraphName());
                project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
            }
            if (project.getTemporalIntegratedGraph() != null) {
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);

                // Retrieve the integrated temporal graph by its graph name and cast it to IntegratedGraphJenaImpl
                Graph integratedGraph = graphStoreInterface.getGraph(project.getTemporalIntegratedGraph().getGraphName());
                project.setTemporalIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return project; // Return the found project (or null if not found)
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
     */
    public void deleteProject(String id) {
        Project p = ormProject.findById(Project.class, id);
        for (DataRepository dr: p.getRepositories()) {
            for (Dataset d: dr.getDatasets()) {
                deleteDatasetFromProject(id, d.getId());
            }
        }
        ormProject.deleteOne(Project.class, id);
    }

    /**
     * Deletes a dataset from the specified project.
     *
     * @param projectId The ID of the project to delete the dataset from.
     * @param datasetId The ID of the dataset to delete.
     * @throws IllegalArgumentException If the project with the given ID is not found.
     */
    public void deleteDatasetFromProject(String projectId, String datasetId) {
        Project project = getProject(projectId);
        List<DataRepository> repositoriesOfProject = project.getRepositories();
        boolean datasetFound = false;

        // Iterate through the data repositories
        for (DataRepository repoInProject : repositoriesOfProject) {
            // Iterate through the datasets in each data repository
            Iterator<Dataset> datasetIterator = repoInProject.getDatasets().iterator();
            while (datasetIterator.hasNext()) {
                Dataset dataset = datasetIterator.next();
                if (datasetId.equals(dataset.getId())) {
                    datasetFound = true;
                    // Remove the dataset from the data repository
                    datasetIterator.remove();
                    project.setRepositories(repositoriesOfProject); // Save and set the updated list of data repositories
                    // Delete rdf file (\jenaFiles)
                    GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);
                    graphStore.deleteGraph(dataset.getLocalGraph());
                    // Remove from Data layer
                    DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
                    dlInterface.deleteDataset(dataset.getUUID());
                    break;
                }
            }
        }
        // Check if the dataset was not found in any data repository
        if (!datasetFound) {
            throw new NoSuchElementException("Dataset not found with id: " + datasetId);
        }
        // Save the updated project
        saveProject(project);
    }

    /**
     * Checks if a project contains a dataset with the given ID.
     *
     * @param projectID      The ID of the project to check.
     * @param datasetID The ID of the dataset to check.
     * @return true if the project contains the dataset, false otherwise.
     */
    public boolean projectContains(String projectID, String datasetID) {
        Project project = getProject(projectID);
        List<DataRepository> repos = project.getRepositories();

        // Iterate through the repositories
        for (DataRepository repo : repos) {
            List<Dataset> datasets = repo.getDatasets();
            // Iterate through the datasets
            for (Dataset dataset : datasets) {
                String datasetId = dataset.getId();
                if (datasetId.equals(datasetID)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the datasets associated with a project.
     *
     * @param id The ID of the project.
     * @return A list of datasets belonging to the project.
     */
    public List<Dataset> getDatasetsOfProject(String id) {
        Project project = getProject(id);
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
     */
    public void putProject(Project project) {
        // Retrieve the original project from the database based on its ID
        Project originalProject = getProject(project.getProjectId());

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
        }
        else {
            throw new NoChangesException("No changes made to the project");
        }
    }

    /**
     * Clones a project, creating a new project with the same structure and data as the original project.
     *
     * @param originalProjectID Identification of the project to be cloned.
     * @return The cloned project.
     */
    public Project cloneProject(String originalProjectID) {
        Project projectToClone = getProject(originalProjectID);
        // Reset the project ID to null to create a new project
        projectToClone.setProjectId(null);
        projectToClone.setProjectName(projectToClone.getProjectName() + " - Copy");

        // Get the list of repositories from the original project
        List<DataRepository> repositoriesToClone = projectToClone.getRepositories();
        if (repositoriesToClone != null && !repositoriesToClone.isEmpty()) {
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
        Project project = getProject(projectId);
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
        Project project = getProject(id);
        return project.getRepositories();
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
        Project project = getProject(projectID);
        project.setIntegratedDatasets(new ArrayList<>());
        saveProject(project);
    }
}

