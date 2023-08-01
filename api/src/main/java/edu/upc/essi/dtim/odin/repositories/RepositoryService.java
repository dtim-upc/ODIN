package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.DataResource;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.project.Project;
import edu.upc.essi.dtim.odin.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class RepositoryService {

    private final AppConfig appConfig;

    public RepositoryService(@Autowired AppConfig appConfig) {
        try {
            this.appConfig = appConfig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<DataRepository> getRepositoriesOfProject(String projectId) {
        ProjectService projectService = new ProjectService(appConfig);
        List<DataResource> dataResources = projectService.findById(projectId).getDataResources();

        List<DataRepository> dataRepositories = new ArrayList<>();

        for(DataResource dataResource : dataResources){
            if(dataResource.getId() == )
        }
    }
}
