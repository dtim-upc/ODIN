package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.DataResource;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalDBRepository;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        List<DataRepository> dataResources = projectService.getProjectById(projectId).getRepositories();

        List<DataRepository> dataRepositories = new ArrayList<>();

        for(DataResource dataResource : dataResources){
            if(dataResource.getId().equals("0")) System.out.println("++++++++++++++++++++++++++++ GET REPOSITORIES");
        }
        DataRepository dr = new RelationalDBRepository();
        ((RelationalDBRepository) dr).setUsername("RAMON DEL REPO");
        dataRepositories.add(dr);

        return dataRepositories;
    }
}
