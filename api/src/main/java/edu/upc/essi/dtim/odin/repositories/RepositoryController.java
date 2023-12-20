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
    @Autowired
    private RepositoryService repositoryService;

    @PostMapping("/test-connection")
    public Boolean testConnection(@RequestBody Map<String, String> requestData) {
        return repositoryService.testConnectionFromRequest(requestData);
    }

    @GetMapping(value = "/{id}/tables")
    public ResponseEntity<Object> retrieveDBTables(@PathVariable("id") String repositoryId) {
        logger.info("Get tables received from repository: " + repositoryId);
        List<TableInfo> table = repositoryService.getDatabaseTablesInfo(repositoryId);
        return ResponseEntity.ok(table);
    }

    @PostMapping(value = "/project/{id}/newRepository")
    public ResponseEntity<Object> postRepository(@PathVariable("id") String projectId,
                                                @RequestBody Map<String, String> repositoryData) {
        logger.info("Post repository received for project " + projectId + " with repo name " + repositoryData.get("repositoryName") + " and type " + repositoryData.get("repositoryType"));
        repositoryService.createRepository(repositoryData, projectId);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


}

