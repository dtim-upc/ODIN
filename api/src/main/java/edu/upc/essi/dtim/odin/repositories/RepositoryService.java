package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.APIRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.FormatNotAcceptedException;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import edu.upc.essi.dtim.odin.repositories.POJOs.TableInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class RepositoryService {
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();
    @Autowired
    private ProjectService projectService;
    @Autowired
    private AppConfig appConfig;

    /**
     * Retrieves a repository by its unique identifier
     *
     * @param repositoryId The unique identifier of the repository to retrieve.
     * @return The repository object.
     */
    public DataRepository getRepositoryById(String repositoryId) {
        DataRepository dataRepository = ormDataResource.findById(DataRepository.class, repositoryId);
        if (dataRepository == null) {
            throw new IllegalArgumentException("Repository not found with repositoryId: " + repositoryId);
        }
        return dataRepository;
    }

    /**
     * Gets all the necessary information from the frontend to attempt a JDBC connection and tries to connect to it, in
     * order to check if the connection is possible.
     *
     * @param requestData information to connect to the database.
     * @return A boolean indicating if the connection was successful.
     */
    public boolean testConnectionFromRequest(Map<String, String> requestData) {
        String url = requestData.get("url");
        String username = requestData.get("username");
        String password = requestData.get("password");
        String port = requestData.get("port");
        String hostname = requestData.get("hostname");
        String databaseName = requestData.get("databaseName");
        String databaseType = requestData.get("databaseType");

        String customUrl = "jdbc:" + databaseType + "://" + hostname + ":" + port + "/" + databaseName;

        return testConnection(url, username, password) || testConnection(customUrl, username, password);
    }

    /**
     * Attempts to connect to a JDBC database
     *
     * @param url      URL of the database.
     * @param user     user used to connect to the database.
     * @param password password belonging to the user.
     * @return A boolean indicating if the connection was successful.
     */
    private boolean testConnection(String url, String user, String password) {
        // Check that all parameters have values
        if (url != null && !url.isEmpty() && user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            RelationalJDBCRepository jdbcRepository = new RelationalJDBCRepository(user, password, url);
            // List<String> tables = jdbcRepository.retrieveTables(); // can be used to check the tables of the db
            return jdbcRepository.testConnection();
        } else {
            return false;
        }
    }

    /**
     * Creates a new DataRepository with the specified repository name, assigns all the necessary parameters and stores it.
     *
     * @param repositoryData Data to create the repository
     * @param projectId      Identification of the project to which the new repository will belong to.
     */
    public void postRepository(Map<String, String> repositoryData, String projectId) {
        DataRepository repository;

        switch (repositoryData.get("repositoryType")) {
            case "RelationalJDBCRepository":
                repository = new RelationalJDBCRepository();
                String url = repositoryData.get("url");
                String username = repositoryData.get("username");
                String password = repositoryData.get("password");

                String port = repositoryData.get("port");
                String hostname = repositoryData.get("hostname");
                String databaseName = repositoryData.get("databaseName");
                String databaseType = repositoryData.get("databaseType");

                String customUrl = "jdbc:" + databaseType + "://" + hostname + ":" + port + "/" + databaseName;

                ((RelationalJDBCRepository) repository).setUsername(username);
                ((RelationalJDBCRepository) repository).setPassword(password);

                if (testConnection(url, username, password)) {
                    ((RelationalJDBCRepository) repository).setUrl(url);
                }
                else if (testConnection(customUrl, username, password)) {
                    ((RelationalJDBCRepository) repository).setUrl(customUrl);
                }
                break;
            case "LocalRepository":
                repository = new LocalRepository();
                ((LocalRepository) repository).setPath(repositoryData.get("path"));
                break;
            case "ApiRepository":
                repository = new APIRepository();
                ((APIRepository) repository).setUrl(repositoryData.get("url"));
                break;
            default:
                repository = new DataRepository();
        }

        repository.setRepositoryName(repositoryData.get("repositoryName"));
        repository.setVirtual(Boolean.valueOf(repositoryData.get("isVirtual")));

        Project project = projectService.getProject(projectId);
        project.getRepositories().add(repository);

        projectService.saveProject(project);
        saveRepository(repository);
    }

    /**
     * Persists a repository in the ODIN database.
     *
     * @param repository Repository to be saved.
     */
    public void saveRepository(DataRepository repository) {
        ormDataResource.save(repository);
    }

    /**
     * Get the information of the tables of a database connection.
     *
     * @param repositoryId Identification of the repository whose tables will be retrieved (the repository has a
     *                     parameter with the database URL, so we can connect to it and extract the information).
     */
    public List<TableInfo> retrieveTablesInfo(String repositoryId) {
        DataRepository repository = getRepositoryById(repositoryId);
        if (!(repository instanceof RelationalJDBCRepository)) {
            throw new FormatNotAcceptedException("Repository is not relational");
        }

        String url = ((RelationalJDBCRepository) repository).getUrl();
        String username = ((RelationalJDBCRepository) repository).getUsername();
        String password = ((RelationalJDBCRepository) repository).getPassword();

        List<TableInfo> tableList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             Statement statementSize = connection.createStatement();
             Statement statementLines = connection.createStatement()) {

            // Get the table names
            String tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';";
            try (ResultSet resultSet = statement.executeQuery(tableQuery)) {
                while (resultSet.next()) {
                    // For each table, get the number of rows of the table
                    String tableName = resultSet.getString("table_name");
                    String rowCountQuery = "SELECT COUNT(*) FROM " + tableName + ";";

                    try (ResultSet rowCountResultSet = statementLines.executeQuery(rowCountQuery)) {
                        if (rowCountResultSet.next()) {
                            int tableRowCount = rowCountResultSet.getInt(1);

                            rowCountResultSet.close();

                            // Size of the table
                            String tableSizeQuery = "SELECT pg_size_pretty(pg_total_relation_size('" + tableName + "')) AS total_size;";
                            try (ResultSet sizeResultSet = statementSize.executeQuery(tableSizeQuery)) {
                                if (sizeResultSet.next()) {
                                    String tableSize = sizeResultSet.getString("total_size");

                                    sizeResultSet.close();

                                    TableInfo tableInfo = new TableInfo(tableName, tableSize, String.valueOf(tableRowCount));
                                    tableList.add(tableInfo);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalServerErrorException("Could not get the data from the database", e.getMessage());
        }
        return tableList;
    }

    /**
     * Deletes a repository from the specified project, and its associated datasets.
     *
     * @param projectId     The ID of the project to delete the repository from.
     * @param repositoryID  The ID of the repository to delete.
     */
    public void deleteRepositoryFromProject(String projectId, String repositoryID) {
        Project project = projectService.getProject(projectId);
        List<DataRepository> repositoriesOfProject = project.getRepositories();
        boolean projectFound = false;
        // Iterate through the data repositories
        for (DataRepository repoInProject : repositoriesOfProject) {
            if (repoInProject.getId().equals(repositoryID)) {
                projectFound = true;
                // Iterate through the datasets in the repository and delete the RDF file and from the Data Layer
                for (Dataset dataset : repoInProject.getDatasets()) {
                    // Delete rdf file (\jenaFiles)
                    GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);
                    graphStore.deleteGraph(dataset.getLocalGraph());
                    // Remove from Data layer
                    DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
                    dlInterface.deleteDatasetFromFormattedZone(dataset.getUUID());
                }
                repositoriesOfProject.remove(repoInProject);
                break;
            }
        }
        project.setRepositories(repositoriesOfProject); // Save and set the updated list of data repositories
        // Throw an exception if the repository was not found
        if (!projectFound) {
            throw new NoSuchElementException("Repository not found with id: " + repositoryID);
        }
        projectService.saveProject(project); // Save the updated project without the repository
    }

    public void editDataset(String repositoryID, String repositoryName) {
        DataRepository originalRepository = getRepositoryById(repositoryID);

        originalRepository.setRepositoryName(repositoryName);

        saveRepository(originalRepository);
    }
}
