package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.DataResource;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.odin.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RepositoryController {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryController.class);

    private final RepositoryService repositoryService;

    RepositoryController(@Autowired RepositoryService repositoryService){
        this.repositoryService = repositoryService;
    }

    @GetMapping("/projects/{id}/repositories")
    public ResponseEntity<List<DataRepository>> getRepositoriesOfProject(@PathVariable("id") String projectId) {
        logger.info("GET request received for retrieving repositories of project "+projectId);
        List<DataRepository> repositoriesOfProject = repositoryService.getRepositoriesOfProject(projectId);
        if (!repositoriesOfProject.isEmpty()) {
            return ResponseEntity.ok(repositoriesOfProject);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
