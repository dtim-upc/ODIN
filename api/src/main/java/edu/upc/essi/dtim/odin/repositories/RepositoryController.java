package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RepositoryController {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);

    private final RepositoryService repositoryService;

    /**
     * Constructor for RepositoryController.
     *
     * @param repositoryService The repository service to be used.
     */
    @Autowired
    RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /**
     * Get the repositories associated with a specific project.
     *
     * @param projectId The ID of the project.
     * @return A ResponseEntity containing the list of repositories if found, or a 404 response if not found.
     */
    @GetMapping("/projects/{id}/repositories")
    public ResponseEntity<List<DataRepository>> getRepositoriesOfProject(@PathVariable("id") String projectId) {
        logger.info("GET request received for retrieving repositories of project " + projectId);

        // Call the service to retrieve repositories associated with the project
        List<DataRepository> repositoriesOfProject = repositoryService.getRepositoriesOfProject(projectId);

        // Check if repositories were found
        if (!repositoriesOfProject.isEmpty()) {
            // Return a successful response with the list of repositories
            return ResponseEntity.ok(repositoriesOfProject);
        } else {
            // Return a 404 response if no repositories were found
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/data-repository-types")
    public List<DataRepositoryTypeInfo> getAllDataRepositoryTypes() {
        return repositoryService.getAllDataRepositoryTypes();
    }
}

