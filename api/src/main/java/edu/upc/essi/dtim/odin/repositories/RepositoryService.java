package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.DataResource;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalDBRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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


    /**
     * Get the repositories associated with a specific project.
     *
     * @param projectId The ID of the project.
     * @return A list of DataRepository objects representing the repositories.
     */
    public List<DataRepository> getRepositoriesOfProject(String projectId) {
        // Create a new instance of the ProjectService using the AppConfig
        ProjectService projectService = new ProjectService(appConfig);

        // Get the list of DataResource objects associated with the project
        List<DataRepository> repositories = projectService.getProjectById(projectId).getRepositories();

        // Create a list to store DataRepository objects
        List<DataRepository> dataRepositories = new ArrayList<>();

        // Iterate through the DataResource objects
        for (DataRepository dataRepository : repositories) {
            // Check if the ID of the DataResource is "0"
            if (dataRepository.getId().equals("0")) {
                System.out.println("++++++++++++++++++++++++++++ GET REPOSITORIES");
            }
        }

        // Create a new RelationalDBRepository and set some properties
        DataRepository dr = new RelationalDBRepository();
        ((RelationalDBRepository) dr).setUsername("RAMON DEL REPO");

        // Add the DataRepository to the list
        dataRepositories.add(dr);

        // Return the list of DataRepository objects
        return dataRepositories;
    }


    public List<DataRepositoryTypeInfo> getAllDataRepositoryTypes() {
        List<Class<? extends DataRepository>> dataRepositoryClasses = Arrays.asList(
                RelationalJDBCRepository.class,
                LocalRepository.class
        );

        List<DataRepositoryTypeInfo> dataRepositoryInfoList = DataRepositoryInfoExtractor.extractDataRepositoryInfo(dataRepositoryClasses);
        return  dataRepositoryInfoList;
    }
}
