package edu.upc.essi.dtim.odin.project;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

@RestController
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    /**
     * Constructs a new ProjectController with the specified ProjectService.
     *
     * @param projectService The ProjectService to be used.
     */
    ProjectController(@Autowired ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Saves a project.
     *
     * @param project The project to save.
     * @return A ResponseEntity containing the saved project and HTTP status 201 (Created).
     */
    @PostMapping("/projects")
    public ResponseEntity<Project> saveProject(@RequestBody Project project) {
        logger.info("POST request received for saving project");
        Project savedProject = projectService.saveProject(project);
        return new ResponseEntity<>(savedProject, HttpStatus.CREATED);
    }

    /**
     * Retrieves a project by its ID.
     *
     * @param id The ID of the project to retrieve.
     * @return A ResponseEntity containing the retrieved project and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if not found.
     */
    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProject(@PathVariable("id") String id) {
        logger.info("GET request received for retrieving project with ID: {}", id);
        Project project = projectService.getProjectById(id);
        if (project.getProjectId() != null) {
            return ResponseEntity.ok(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves all projects.
     *
     * @return A ResponseEntity containing a list of all projects and HTTP status 200 (OK).
     */
    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        logger.info("GET request received for retrieving all projects");
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id The ID of the project to delete.
     * @return A ResponseEntity with HTTP status 200 (OK) and the boolean value true if the project was deleted,
     * or HTTP status 404 (Not Found) if the project was not found.
     */
    @DeleteMapping("/deleteProject/{id}")
    public ResponseEntity<Boolean> deleteProject(@PathVariable("id") String id) {
        logger.info("DELETE request received for deleting project with ID: {}", id);

        // Call the projectService to delete the project and get the result
        boolean deleted = projectService.deleteProject(id);

        // Check if the project was deleted successfully
        if (deleted) {
            // Return a ResponseEntity with HTTP status 200 (OK) and the boolean value true
            return ResponseEntity.ok(true);
        } else {
            // Return a ResponseEntity with HTTP status 404 (Not Found)
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Edits a project.
     *
     * @param project The project to edit.
     * @return A ResponseEntity with HTTP status 200 (OK) and the boolean value true if the project was edited,
     * or HTTP status 404 (Not Found) if the project was not found.
     */
    @PostMapping("/editProject")
    public ResponseEntity<Boolean> editProject(@RequestBody Project project) {
        logger.info("EDIT request received for editing project with ID: {}", project.getProjectId());
        logger.info("EDIT request received for editing project with ID: {}", project.getProjectName());

        // Call the projectService to edit the project and get the result
        boolean edited = projectService.editProject(project);

        // Check if the project was edited successfully
        if (edited) {
            // Return a ResponseEntity with HTTP status 200 (OK) and the boolean value true
            return ResponseEntity.ok(true);
        } else {
            // Return a ResponseEntity with HTTP status 404 (Not Found)
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Clones a project by its ID.
     *
     * @param projectId The ID of the project to clone.
     * @return A ResponseEntity containing the cloned project and HTTP status 201 (Created) if successful,
     * or HTTP status 304 (Not Modified) if the project was not cloned.
     */
    @PostMapping("/cloneProject/{projectId}")
    public ResponseEntity<Project> cloneProject(@PathVariable("id") String projectId) {
        logger.info("Clone request received for cloning project with id: " +  projectId);

        // Call the projectService to clone the project and get the result
        Project projectToClone = projectService.getProjectById(projectId);
        Project projectClone = projectService.cloneProject(projectToClone);

        if (!projectClone.getProjectId().equals(projectId)) {
            return new ResponseEntity<>(projectClone, HttpStatus.CREATED); // HTTP status 201 (Created)
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_MODIFIED); // HTTP status 304 (Not Modified)
        }
    }

    /**
     * Downloads the project schema in Turtle (TTL) format.
     *
     * @param projectID The ID of the project for which the schema will be downloaded.
     * @return A ResponseEntity containing the input stream resource and necessary headers for the download.
     */
    @GetMapping("/project/{id}/download/projectschema")
    public ResponseEntity<InputStreamResource> downloadProjectSchema(
            @PathVariable("id") String projectID
    ) {
        Project project = projectService.getProjectById(projectID);

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

    /**
     * Get the repositories associated with a specific project.
     *
     * @param projectId The ID of the project.
     * @return A ResponseEntity containing the list of repositories if found, or a 404 response if not found.
     */
    @GetMapping("/project/{id}/repositories")
    public ResponseEntity<List<DataRepository>> getRepositoriesOfProject(@PathVariable("id") String projectId) {
        logger.info("GET request received for retrieving repositories of project " + projectId);

        // Call the service to retrieve repositories associated with the project
        List<DataRepository> repositoriesOfProject = projectService.getRepositoriesOfProject(projectId);

        // Check if repositories were found
        if (!repositoriesOfProject.isEmpty()) {
            // Return a successful response with the list of repositories
            return ResponseEntity.ok(repositoriesOfProject);
        } else {
            // Return a 404 response if no repositories were found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves all datasets from a specific project.
     *
     * @param projectId The ID of the project to retrieve datasets from.
     * @return A ResponseEntity object containing the list of datasets or an error message.
     */
    @GetMapping("/project/{projectId}/datasources")
    public ResponseEntity<Object> getDatasetsFromProject(@PathVariable String projectId) {
        logger.info("Get all datasets from project " + projectId);
        try {
            List<Dataset> datasets = projectService.getDatasetsOfProject(projectId);
            if (datasets.isEmpty()) {
                return new ResponseEntity<>("The project does not contain datasets", HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(datasets, HttpStatus.OK); // Return the datasets
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
