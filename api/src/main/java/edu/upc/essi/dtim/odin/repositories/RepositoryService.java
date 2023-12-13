package edu.upc.essi.dtim.odin.repositories;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.ApiRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.project.ProjectService;
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

@Service
public class RepositoryService {
    private final ORMStoreInterface ormDataResource;
    private final ProjectService projectService;

    public RepositoryService(@Autowired ProjectService projectService) {
        try {
            this.ormDataResource = ORMStoreFactory.getInstance();
            this.projectService = projectService;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DataRepository getRepositoryById(String repositoryId) {
        DataRepository dataRepository = ormDataResource.findById(DataRepository.class, repositoryId);
        if (dataRepository == null) {
            throw new IllegalArgumentException("Repository not found with repositoryId: " + repositoryId);
        }
        return dataRepository;
    }

    public boolean testConnection(String url, String user, String password) {
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
     * Creates a new DataRepository with the specified repository name using the ORMStoreInterface.
     *
     * @param repositoryData Data to create the repository
     * @return The created DataRepository.
     */
    public DataRepository createRepository(Map<String, String> repositoryData) {
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
                repository = new ApiRepository();
                ((ApiRepository) repository).setUrl(repositoryData.get("url"));
                break;
            default:
                repository = new DataRepository();
        }

        repository.setRepositoryName(repositoryData.get("repositoryName"));
        repository.setVirtual(Boolean.valueOf(repositoryData.get("isVirtual")));
        return ormDataResource.save(repository); // Save the DataRepository and return it
    }

    /**
     * Adds a DataRepository to a specific project using the ORMStoreInterface and ProjectService.
     *
     * @param projectId  The ID of the project to which the repository will be added.
     * @param repository Repository to be added to the project
     */
    public void addRepositoryToProject(String projectId, DataRepository repository) {
        projectService.addRepositoryToProject(projectId, repository);
    }

    public List<String> getDatabaseTables(String repositoryId) {
        DataRepository repository = getRepositoryById(repositoryId);

        if (repository instanceof RelationalJDBCRepository) {
            return ((RelationalJDBCRepository) repository).retrieveTables();
        } else {
            throw new IllegalArgumentException("Repository is not relational");
        }
    }

    public List<TableInfo> getDatabaseTablesInfo(String repositoryId) {
        DataRepository repository = getRepositoryById(repositoryId);

        if (repository instanceof RelationalJDBCRepository) {
            return retrieveTablesInfo(((RelationalJDBCRepository) repository).getUrl(), ((RelationalJDBCRepository) repository).getUsername(), ((RelationalJDBCRepository) repository).getPassword());
        } else {
            throw new IllegalArgumentException("Repository is not relational");
        }
    }

    public List<TableInfo> retrieveTablesInfo(String url, String username, String password) {
        List<TableInfo> tableList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             Statement statementSize = connection.createStatement();
             Statement statementLines = connection.createStatement();) {

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
            e.printStackTrace();
        }

        return tableList;
    }

}
