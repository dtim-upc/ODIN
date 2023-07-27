package edu.upc.essi.dtim.odin.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ProjectController(@Autowired ProjectService projectService){
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
     *         or HTTP status 404 (Not Found) if not found.
     */
    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProject(@PathVariable("id") String id) {
        logger.info("GET request received for retrieving project with ID: {}", id);
        Project project = projectService.findById(id);
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
     *         or HTTP status 404 (Not Found) if the project was not found.
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
}
