package edu.upc.essi.dtim.odin.projects;

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
    @Autowired
    private ProjectService projectService;

    /**
     * Saves a project.
     *
     * @param project The project to save.
     * @return A ResponseEntity containing the saved project and HTTP status 201 (Created).
     */
    @PostMapping("/projects")
    public ResponseEntity<Project> postProject(@RequestBody Project project) {
        logger.info("Post request received for creating project");
        Project savedProject = projectService.saveProject(project);
        return new ResponseEntity<>(savedProject, HttpStatus.CREATED);
    }

    /**
     * Retrieves a project by its ID.
     *
     * @param projectID The ID of the project to retrieve.
     * @return A ResponseEntity containing the retrieved project and HTTP status 200 (OK) if found,
     * or HTTP status 404 (Not Found) if not found.
     */
    @GetMapping("/projects/{projectID}")
    public ResponseEntity<Project> getProject(@PathVariable("projectID") String projectID) {
        logger.info("Get request received for retrieving project with ID: " + projectID);
        Project project = projectService.getProject(projectID);
        return new ResponseEntity<>(project, HttpStatus.OK);
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
        return new ResponseEntity<>(projects, HttpStatus.OK);
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
        projectService.deleteProject(id);
        return ResponseEntity.ok(true);
    }

    /**
     * Edits a project.
     *
     * @param project The project to edit.
     * @return A ResponseEntity with HTTP status 200 (OK) and the boolean value true if the project was edited,
     * or HTTP status 404 (Not Found) if the project was not found.
     */
    @PostMapping("/editProject")
    public ResponseEntity<Boolean> putProject(@RequestBody Project project) {
        logger.info("EDIT request received for editing project with ID: " + project.getProjectId());
        projectService.putProject(project);
        return ResponseEntity.ok(true);
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
        Project projectClone = projectService.cloneProject(projectId);
        return new ResponseEntity<>(projectClone, HttpStatus.CREATED); // HTTP status 201 (Created)

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
        Project project = projectService.getProject(projectID);

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
        List<DataRepository> repositoriesOfProject = projectService.getRepositoriesOfProject(projectId);
        return ResponseEntity.ok(repositoriesOfProject);
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
        List<Dataset> datasets = projectService.getDatasetsOfProject(projectId);
        return new ResponseEntity<>(datasets, HttpStatus.OK);
    }
}
