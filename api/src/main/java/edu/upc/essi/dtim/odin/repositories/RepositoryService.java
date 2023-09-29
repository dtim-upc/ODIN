package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
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

    /**
     * The ORMStoreInterface dependency for storing datasets.
     */
    private final ORMStoreInterface ormDataResource;
    private final ProjectService projectService;

    public RepositoryService(@Autowired AppConfig appConfig,
                             @Autowired ProjectService projectService) {
        try {
            this.appConfig = appConfig;
            this.ormDataResource = ORMStoreFactory.getInstance();
            this.projectService = projectService;
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
        DataRepository dr = new RelationalJDBCRepository();
        ((RelationalJDBCRepository) dr).setUsername("RAMON DEL REPO");

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

        return DataRepositoryInfoExtractor.extractDataRepositoryInfo(dataRepositoryClasses);
    }

    public boolean testConnection(String url, String user, String password) {
        // Imprimir los parámetros recibidos por consola
        System.out.println("URL: " + url);
        System.out.println("Usuario: " + user);
        System.out.println("Contraseña: " + password);

        // Comprobar si todos los parámetros tienen valor
        if (url != null && !url.isEmpty() && user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            RelationalJDBCRepository jdbcRepository = new RelationalJDBCRepository(user, password, url);
            return jdbcRepository.testConnection();
        } else {
            return false; // Al menos uno de los parámetros no tiene valor, retornar false
        }
    }

    /**
     * Creates a new DataRepository with the specified repository name using the ORMStoreInterface.
     *
     * @param repositoryName The name of the DataRepository to create.
     * @return The created DataRepository.
     */
    public DataRepository createRepository(String repositoryName) {
        // Create a new DataRepository instance
        DataRepository dataRepository = new DataRepository();

        // Set the repository name for the DataRepository
        dataRepository.setRepositoryName(repositoryName);

        // Save the DataRepository and return it
        return ormDataResource.save(dataRepository);
    }

    /**
     * Adds a DataRepository to a specific project using the ORMStoreInterface and ProjectService.
     *
     * @param projectId    The ID of the project to which the repository will be added.
     * @param repositoryId The ID of the DataRepository to be added to the project.
     */
    public void addRepositoryToProject(String projectId, String repositoryId) {
        // Retrieve the DataRepository using its ID
        DataRepository dataRepository = ormDataResource.findById(DataRepository.class, repositoryId);

        // Call the ProjectService to add the repository to the specified project
        projectService.addRepositoryToProject(projectId, repositoryId);
    }
}
