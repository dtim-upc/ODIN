package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/test-connection")
    public Boolean testConnection(@RequestBody Map<String, String> requestData) {
        // Extract data from the request body
        String port = requestData.get("port");
        String username = requestData.get("username");
        String password = requestData.get("password");

        return repositoryService.testConnection(port, username, password);
    }

    @PostMapping(value = "/project/{id}/newRepository")
    public ResponseEntity<Object> addRepository(@PathVariable("id") String projectId,
                                                @RequestParam String repositoryName,
                                                @RequestParam(required = false) String datasetDescription)
    {
        try{
            logger.info("POST REPOSITORY RECEIVED FOR " + projectId);
            // Validate and authenticate access here
            //future check when adding authentification

            // Find/create repository
            DataRepository repository;

            // Create a new repository and add it to the project
            repository = repositoryService.createRepository(repositoryName);
            repositoryService.addRepositoryToProject(projectId, repository.getId());

            // Return success message
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repository not created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the data source");
        }
    }


}

