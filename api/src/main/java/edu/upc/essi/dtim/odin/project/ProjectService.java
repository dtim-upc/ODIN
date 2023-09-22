package edu.upc.essi.dtim.odin.project;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
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
        System.out.println("++++++++++++++++++++ DELETE DATASET OF PROJECTo");

        // Check if the project is found
        if (project == null) {
            System.out.println("++++++++++++++++++++ QUÉ PASO NO ENCONTRÉ");

            // Throw an IllegalArgumentException if the project is not found
            throw new IllegalArgumentException("Project not found");
        }

        // Get the list of data repositories associated with the project
        List<DataRepository> repositoriesOfProjectToUpload = project.getRepositories();
        boolean datasetFound = false;

        // Create an iterator for the data repositories
        Iterator<DataRepository> repositoryIterator = repositoriesOfProjectToUpload.iterator();

        // Iterate through the data repositories using the iterator
        while (repositoryIterator.hasNext()) {
            DataRepository repoInProject = repositoryIterator.next();

            // Iterate through the datasets in each data repository
            Iterator<Dataset> datasetIterator = repoInProject.getDatasets().iterator();
            while (datasetIterator.hasNext()) {
                Dataset dataset = datasetIterator.next();

                // Check if the dataset ID matches the specified dataset ID
                if (datasetId.equals(dataset.getId())) {
                    System.out.println("++++++++++++++++++++encontrado");
                    datasetFound = true;

                    // Remove the dataset from the data repository
                    datasetIterator.remove();

                    // Check if the data repository is now empty and remove it from the project
                    if (repoInProject.getDatasets().isEmpty()) {
                        repositoryIterator.remove();
                    }

                    // Save the updated list of data repositories
                    ormProject.save(repositoriesOfProjectToUpload);

                    // Update the project's list of data repositories
                    project.setRepositories(repositoriesOfProjectToUpload);

                    // Exit the loop after deleting the dataset
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
        // Save the project using the ORM store and get the saved project
        Project savedProject = ormProject.save(project);

        // Check if the project has an integrated graph and a graph name
        if (savedProject.getIntegratedGraph() != null && savedProject.getIntegratedGraph().getGraphName() != null) {
            try {
                // Get the GraphStore interface using the AppConfig
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);

                // Get the integrated graph from the project
                Graph graph = project.getIntegratedGraph();

                // Set the graph name to match the saved project's integrated graph name
                graph.setGraphName(savedProject.getIntegratedGraph().getGraphName() == null ? "noName" : savedProject.getIntegratedGraph().getGraphName());

                // Save the graph to the graph store
                graphStoreInterface.saveGraph(graph);
            } catch (Exception e) {
                // Handle any exceptions by throwing a runtime exception
                throw new RuntimeException(e);
            }
        }

        // Return the saved project
        return savedProject;
    }

    /**
     * Finds a project by its ID.
     *
     * @param projectId The ID of the project to find.
     * @return The found project, or null if not found.
     */
    public Project getProjectById(String projectId) {
        // Print the project ID for debugging
        System.out.println(projectId + " ++++++++++++++++++++++++++++++++projectId");

        // Retrieve the project with the specified ID from the ORM store
        Project project = ormProject.findById(Project.class, projectId);

        // Check if the project has an integrated graph
        try {
            if (project.getIntegratedGraph() != null) {
                // Get the GraphStoreInterface from the GraphStoreFactory using the appConfig
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);

                // Retrieve the integrated graph by its graph name and cast it to IntegratedGraphJenaImpl
                Graph integratedGraph = graphStoreInterface.getGraph(project.getIntegratedGraph().getGraphName());
                project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
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
        return ormProject.deleteOne(Project.class, id);
    }

    /**
     * Checks if a project contains a dataset with the given ID.
     *
     * @param projectId The ID of the project to check.
     * @param
     * dataresourceId The ID of the dataset to check.
     * @return true if the project contains the dataset, false otherwise.
     */
    public boolean projectContains(String projectId, String dataresourceId) {
        // Retrieve the project by its ID from the database
        Project project = getProjectById(projectId);

        // Get the list of repositories in the project
        List<DataRepository> repos = project.getRepositories();

        // Print debugging information
        System.out.println("++++++++++++++++++++ llegue " + repos.size() + repos);

        // Iterate through the repositories
        for (int i = 0; i < repos.size(); ++i) {
            // Get the datasets associated with the current repository
            List<Dataset> datasets = repos.get(i).getDatasets();

            // Print debugging information
            System.out.println("++++++++++++++++++++ entro " + i + " " + datasets.size());

            // Iterate through the datasets
            for (Dataset dataset : datasets) {
                // Get the ID of the current dataset
                String datasetId = dataset.getId();

                // Print debugging information
                System.out.println("++++++++++++++++++++ MIRO " + datasetId + " " + dataresourceId);

                // Check if the dataset ID matches the provided dataresourceId
                if (datasetId.equals(dataresourceId)) {
                    // Dataset with the specified ID found in the project
                    System.out.println("++++++++++++++++++++ ENCONTRÉ MUCHACHO");
                    return true; // Return true if the dataresourceId exists in any dataset
                }
            }
        }

        // Dataset with the specified ID not found in the project
        System.out.println("++++++++++++++++++++ NO ENCONTRÉ MUCHACHO");
        return false; // Return false if the dataresourceId is not found in any dataset
    }

    /**
     * Retrieves the datasets associated with a project.
     *
     * @param id The ID of the project.
     * @return A list of datasets belonging to the project.
     */
    public List<Dataset> getDatasetsOfProject(String id) {
        // Retrieve the project by its ID from the database
        Project project = ormProject.findById(Project.class, id);

        // Create a list to store the datasets associated with the project
        List<Dataset> datasets = new ArrayList<>();

        // Iterate through the repositories in the project and collect their datasets
        for (DataRepository repository : project.getRepositories()){
            datasets.addAll(repository.getDatasets());
        }

        return datasets; // Return the list of datasets associated with the project
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
        projectToClone.setProjectName(projectToClone.getProjectName()+" - Copy");

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
     * @param repositoryId The ID of the repository to be added.
     * @throws IllegalArgumentException If the project with the given ID is not found.
     */
    public void addRepositoryToProject(String projectId, String repositoryId) {
        // Retrieve the project with the given ID
        Project project = getProjectById(projectId);

        // If the project is not found, throw an exception
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        // Retrieve the data repository by its ID
        DataRepository dataRepository = ormProject.findById(DataRepository.class, repositoryId);

        // Add the data repository to the project's list of repositories
        project.getRepositories().add(dataRepository);

        // Save the project to persist the changes
        saveProject(project);
    }

    /**
     * Retrieves the list of data repositories associated with a project.
     *
     * @param id The ID of the project.
     * @return A list of DataRepository objects belonging to the project.
     */
    public List<DataRepository> getRepositoriesOfProject(String id) {
        // Find the project by its ID
        Project project = ormProject.findById(Project.class, id);

        // Return the list of data repositories associated with the project
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
        // Retrieve the project by its ID
        Project project = getProjectById(projectID);

        // Retrieve the dataset by its ID
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
        // 1. Recupera el proyecto por su ID
        Project project = getProjectById(projectID);

        if (project != null) {
            // 2. Comprueba si el dataset ya está en la lista de datasets integrados del proyecto
            List<Dataset> integratedDatasets = project.getIntegratedDatasets();
            if (isDatasetIntegrated(integratedDatasets, datasetID)){
                return project;
            } else {
                // 3. Recupera el dataset por su ID
                Dataset dataset = ormProject.findById(Dataset.class, datasetID);

                if (dataset != null) {
                    // 4. Agrega el nuevo dataset a la lista de datasets integrados
                    integratedDatasets.add(dataset);

                    // 5. Actualiza la lista de datasets integrados en el proyecto
                    project.setIntegratedDatasets(integratedDatasets);

                    // 6. Guarda el proyecto para persistir los cambios
                    return saveProject(project);
                }
            }
        }
        return null; // Proyecto no encontrado o dataset no encontrado
    }

    // Función auxiliar para comprobar si un dataset ya está integrado en un proyecto
    private boolean isDatasetIntegrated(List<Dataset> integratedDatasets, String datasetID) {
        for (Dataset integratedDataset : integratedDatasets) {
            if (integratedDataset.getId().equals(datasetID)) {
                return true;
            }
        }
        return false;
    }

    public Project deleteIntegratedDatasets(String projectID) {
        // 1. Recupera el proyecto por su ID
        Project project = getProjectById(projectID);

        if (project != null) {
            project.setIntegratedDatasets(new ArrayList<>());
            System.out.println(project.getIntegratedDatasets());
            System.out.println("+++++++++++++++++++++++ELIMINADOS EXTERMINADOSSSSS");
            return saveProject(project);
        }
        return null; // Proyecto no encontrado o dataset no encontrado
    }
}

