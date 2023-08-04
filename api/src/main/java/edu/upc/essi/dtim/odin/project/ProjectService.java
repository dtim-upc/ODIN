package edu.upc.essi.dtim.odin.project;

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
import java.util.List;

@Service
public class ProjectService {
    ORMStoreInterface ormProject;
    private AppConfig appConfig;

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
        Project project = getProjectById(projectId);
        System.out.println("++++++++++++++++++++ DELETE DATASET OF PROJECTo");

        if (project == null) {
            System.out.println("++++++++++++++++++++ QUÉ PASO NO ENCONTRÉ");

            throw new IllegalArgumentException("Project not found");
        }

        List<DataRepository> dataresourcesOfProjectToUpload = project.getRepositories();
        boolean datasetFound = false;
        if(!dataresourcesOfProjectToUpload.isEmpty()) {
            for (DataRepository repoInProject : dataresourcesOfProjectToUpload) {
                for (Dataset dataset : repoInProject.getDatasets()) {
                    if (datasetId.equals(dataset.getId())) {
                        System.out.println("++++++++++++++++++++encontrado");
                        datasetFound = true;
                        repoInProject.removeDataset(dataset);
                        ormProject.save(dataresourcesOfProjectToUpload);

                        // Agregamos el código para verificar si el repositorio está vacío y, de ser así, eliminarlo del proyecto.
                        if (repoInProject.getDatasets().isEmpty()) {
                            dataresourcesOfProjectToUpload.remove(repoInProject);
                        }

                        // Agregamos el código para buscar el repositorio actualizado que contiene el dataset eliminado y reemplazarlo en la lista
                        /*
                        for (int i = 0; i < dataresourcesOfProjectToUpload.size(); i++) {
                            DataRepository updatedRepo = dataresourcesOfProjectToUpload.get(i);
                            if (updatedRepo.getId().equals(repoInProject.getId())) {
                                // Encontramos el repositorio actualizado que coincide con el repositorio eliminado
                                dataresourcesOfProjectToUpload.set(i, repoInProject);
                                break;
                            }
                        }

                         */

                        project.setRepositories(dataresourcesOfProjectToUpload);
                        break; // Rompemos el bucle después de eliminar el objeto
                    }
                }
            }
        }

        if(!datasetFound) {
            throw new IllegalArgumentException("Dataset not found");
        }
        saveProject(project);
    }

    /**
     * Saves a project.
     *
     * @param project The project to save.
     * @return The saved project.
     */
    public Project saveProject(Project project) {
        Project savedProject = ormProject.save(project);
        if(savedProject.getIntegratedGraph() != null){
            if(savedProject.getIntegratedGraph().getGraphName() != null) {
                try {
                    GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
                    Graph graph = project.getIntegratedGraph();
                    graph.setGraphName(savedProject.getIntegratedGraph().getGraphName());
                    graphStoreInterface.saveGraph(graph);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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
        System.out.println(projectId+" ++++++++++++++++++++++++++++++++projectId");
        Project project = ormProject.findById(Project.class, projectId);

        //debemos cargar también el contenido de las triplas de la relación con el grafo
        try {
            if(project.getIntegratedGraph() != null) {
                GraphStoreInterface graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
                Graph integratedGraph = graphStoreInterface.getGraph(project.getIntegratedGraph().getGraphName());
                project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
     * @param dataresourceId The ID of the dataset to check.
     * @return true if the project contains the dataset, false otherwise.
     */
    public boolean projectContains(String projectId, String dataresourceId) {
        Project project = ormProject.findById(Project.class, projectId);
        List<DataRepository> repos = project.getRepositories();
        System.out.println("++++++++++++++++++++ llegue "+repos.size()+repos.toString());

        for (int i =0 ; i<repos.size(); ++i) {
            List<Dataset> datasets = repos.get(i).getDatasets();
            System.out.println("++++++++++++++++++++ entro "+i+" "+datasets.size());
            // Aquí puedes agregar el código para verificar si dataresourceId existe en los datasets
            for (int j =0 ; j<datasets.size(); ++j) {
                System.out.println("++++++++++++++++++++ MIRO "+datasets.get(j).getId()+" "+dataresourceId);
                if (datasets.get(j).getId().equals(dataresourceId)) {
                    System.out.println("++++++++++++++++++++ ENCONTRÉ MUCHACHO");
                    return true; // Si el dataresourceId existe en algún dataset, devuelve true
                }
            }
        }
        System.out.println("++++++++++++++++++++ NO ENCONTRÉ MUCHACHO");
        return false; // Si no se encontró el dataresourceId en ningún dataset, devuelve false
    }

    /**
     * Retrieves the datasets of a project.
     *
     * @param id The ID of the project.
     * @return A list of datasets belonging to the project.
     */
    public List<Dataset> getDatasetsOfProject(String id) {
        Project project = ormProject.findById(Project.class, id);
        List<Dataset> datasets = new ArrayList<>();
        for (DataRepository repository : project.getRepositories()){
            datasets.addAll(repository.getDatasets());
        }
        return datasets;
    }

    public boolean editProject(Project project) {
        Project originalProject = ormProject.findById(Project.class, project.getProjectId());

        // Check if any attribute has changed
        if (!project.getProjectName().equals(originalProject.getProjectName())
                || !project.getProjectDescription().equals(originalProject.getProjectDescription())
                || !project.getProjectColor().equals(originalProject.getProjectColor())
                || !project.getProjectPrivacy().equals(originalProject.getProjectPrivacy())
        ) {
            // At least one attribute has changed
            originalProject.setProjectName(project.getProjectName());
            originalProject.setProjectDescription(project.getProjectDescription());
            originalProject.setProjectColor(project.getProjectColor());
            originalProject.setProjectPrivacy(project.getProjectPrivacy());

            // Perform the database update operation to save the changes
            saveProject(originalProject);

            return true;
        }

        // No changes detected, return false
        return false;
    }

    public Project cloneProject(Project projectToClone) {
        projectToClone.setProjectId(null);

        // Get the repositories from the original project
        List<DataRepository> repositoriesToClone = projectToClone.getRepositories();

        if (repositoriesToClone != null && !repositoriesToClone.isEmpty()) {
            // Create a new list to store the cloned repositories
            List<DataRepository> clonedRepositories = new ArrayList<>();

            for (DataRepository repositoryToClone : repositoriesToClone) {
                repositoryToClone.setId(null);

                // Get the datasets from the original repository
                List<Dataset> datasetsToClone = repositoryToClone.getDatasets();

                if (datasetsToClone != null && !datasetsToClone.isEmpty()) {
                    // Create a new list to store the cloned datasets
                    List<Dataset> clonedDatasets = new ArrayList<>();

                    for (Dataset datasetToClone : datasetsToClone) {
                        datasetToClone.setId(null);
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

        if (projectToClone.getIntegratedGraph() != null) {
            projectToClone.getIntegratedGraph().setGraphName(null);
        }

        return saveProject(projectToClone);
    }

    public void addRepositoryToProject(String projectId, String repositoryId) {
        // Retrieve the project with the given ID
        Project project = getProjectById(projectId);

        // If the project is not found, throw an exception
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        DataRepository dataResource = ormProject.findById(DataRepository.class, repositoryId);

        project.getRepositories().add(dataResource);

        saveProject(project);
    }

    public List<DataRepository> getRepositoriesOfProject(String id) {
        Project project = ormProject.findById(Project.class, id);
        return project.getRepositories();
    }
}

