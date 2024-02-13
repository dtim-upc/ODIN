package edu.upc.essi.dtim.odin.projects;

import edu.upc.essi.dtim.NextiaCore.repositories.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.odin.nextiaStore.graphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.graphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {
    ORMStoreInterface ormProject = ORMStoreFactory.getInstance();
    @Autowired
    private  AppConfig appConfig;

    // ---------------- CRUD/ORM operations

    /**
     * Saves a project into the ORM. (Also serves as post operation)
     *
     * @param project The project to save.
     * @return The saved project.
     */
    public Project saveProject(Project project) {
        // For some reason, ormProject.save(project) does not store the global graph of the integrated graphs, so if we
        // want to obtain it, we need to execute the function getProject(), which regenerates the global graphs
        Project savedProject = ormProject.save(project); // Save the project using the ORM store

        // Check if the project has an integrated and/or temporal integrated graph. If that is the case, set the name
        // of the RDF file (the number) as the project's graph name, so we can access it later.
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
                // Set the graph name to match the saved project's temporal integrated graph name
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
     * @return The found project.
     */
    public Project getProject(String projectId) {
        // Retrieve the project with the specified ID from the ORM store
        Project project = ormProject.findById(Project.class, projectId);
        if (project == null) {
            throw new ElementNotFoundException("Project not found with ID: " + projectId);
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

        return project;
    }

    /**
     * Retrieves all projects.
     *
     * @return A list of all projects.
     */
    public List<Project> getAllProjects() {
        // We do not need all the data that is gathered in the getProject function, as we only want to show the name
        // of the project. Once a user selects a project, then all the information about graphs and so is gathered
        return ormProject.getAll(Project.class);
    }

    /**
     * Edits a project by updating its attributes if they have changed.
     *
     * @param project The modified project with updated attributes.
     */
    public void putProject(Project project) {
        Project originalProject = getProject(project.getProjectId());

        originalProject.setProjectName(project.getProjectName());
        originalProject.setProjectDescription(project.getProjectDescription());
        originalProject.setProjectColor(project.getProjectColor());
        originalProject.setProjectPrivacy(project.getProjectPrivacy());

        saveProject(originalProject); // Perform the database update operation to save the changes
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
                // Delete rdf file (\jenaFiles)
                GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);
                graphStore.deleteGraph(d.getLocalGraph());
                // Remove from Data layer
                DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
                dlInterface.deleteDatasetFromFormattedZone(d.getUUID());
                // Both the dataset and the repository in the ORM store are deleted when the project is deleted,
                // so no need to do that manually
            }
        }
        ormProject.deleteOne(Project.class, id);
    }

    // ---------------- Other operations

    // TODO: Remake this
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
     * Downloads the project schema in Turtle (TTL) format.
     *
     * @param projectID The ID of the project for which the schema will be downloaded.
     * @return A ResponseEntity containing the input stream resource and necessary headers for the download.
     */
    public ResponseEntity<InputStreamResource> downloadProjectSchema(String projectID) {
        Project project = getProject(projectID);

        Model model = project.getIntegratedGraph().getGraph();
        StringWriter writer = new StringWriter();
        model.write(writer, "TTL");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + project.getProjectName() + ".ttl");

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(writer.toString().getBytes()));

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/turtle"))
                .body(resource);
    }

    public void resetProjectSchema(String projectID) {
        Project project = getProject(projectID);

        project.setIntegratedDatasets(new ArrayList<>());
        project.setIntegratedGraph(null);

        saveProject(project);
    }
}