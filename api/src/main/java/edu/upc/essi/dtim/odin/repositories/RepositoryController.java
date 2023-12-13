package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.odin.repositories.POJOs.TableInfo;
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

    @PostMapping("/test-connection")
    public Boolean testConnection(@RequestBody Map<String, String> requestData) {
        String url = requestData.get("url");
        String username = requestData.get("username");
        String password = requestData.get("password");
        String port = requestData.get("port");
        String hostname = requestData.get("hostname");
        String databaseName = requestData.get("databaseName");
        String databaseType = requestData.get("databaseType");

        String customUrl = "jdbc:" + databaseType + "://" + hostname + ":" + port + "/" + databaseName;

        return repositoryService.testConnection(url, username, password) || repositoryService.testConnection(customUrl, username, password);
    }

    @GetMapping(value = "/{id}/tables")
    public ResponseEntity<Object> retrieveDBtables(@PathVariable("id") String repositoryId) {
        logger.info("Get tables received from repository: " + repositoryId);

        try {
            List<TableInfo> table = repositoryService.getDatabaseTablesInfo(repositoryId);

            return ResponseEntity.ok(table); // Return the results as a JSON
        } catch (Exception e) {
            // TODO: manage exceptions
            return ResponseEntity.status(500).body("");
        }
    }

    @PostMapping(value = "/project/{id}/newRepository")
    public ResponseEntity<Object> addRepository(@PathVariable("id") String projectId,
                                                @RequestBody Map<String, String> repositoryData) {
        try {
            logger.info("Post repository received for project " + projectId + " with repo name " + repositoryData.get("repositoryName") + " and type " + repositoryData.get("repositoryType"));

            DataRepository repository = repositoryService.createRepository(repositoryData);
            repositoryService.addRepositoryToProject(projectId, repository);

            return new ResponseEntity<>(null, HttpStatus.OK); // Return success message
        } catch (UnsupportedOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Repository not created successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the data source");
        }
    }


}

