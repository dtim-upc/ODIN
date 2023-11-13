package edu.upc.essi.dtim.odin.repositories;

import com.google.protobuf.Api;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.ApiRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
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

    public boolean testConnection(String url, String user, String password) {
        // Imprimir los parámetros recibidos por consola
        System.out.println("URL: " + url);
        System.out.println("Usuario: " + user);
        System.out.println("Contraseña: " + password);

        // Comprobar si todos los parámetros tienen valor
        if (url != null && !url.isEmpty() && user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            RelationalJDBCRepository jdbcRepository = new RelationalJDBCRepository(user, password, url);
            List<String> tables = jdbcRepository.retrieveTables();

            if (jdbcRepository.testConnection()) {
                // Imprimir los nombres de las tablas
                System.out.println("Tablas en la base de datos:");
                for (String tableName : tables) {
                    System.out.println(tableName);
                }
                return true;
            } else {
                return false;
            }
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
    public DataRepository createRepository(String repositoryName, String repositoryType) {
        // Create a new DataRepository instance
        DataRepository repository;



        switch (repositoryType){
            case "RelationalJDBCRepository":
                repository = new RelationalJDBCRepository();
                break;
            case "LocalRepository":
                repository = new LocalRepository();
                break;
            case "ApiRepository":
                repository = new ApiRepository();
                break;
            default:
                repository = new DataRepository();
        }

        // Set the repository name for the DataRepository
        repository.setRepositoryName(repositoryName);

        // Save the DataRepository and return it
        return ormDataResource.save(repository);
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

    public DataRepository addRepositoryParameters(String repositoryId, Map<String, String> requestData) {
        DataRepository repository = ormDataResource.findById(DataRepository.class, repositoryId);

        if (repository != null) {
            if (repository instanceof RelationalJDBCRepository) {
                String url = requestData.get("url");
                String username = requestData.get("username");
                String password = requestData.get("password");

                String port = requestData.get("port");
                String hostname = requestData.get("hostname");
                String databasename = requestData.get("databaseName");
                String databaseType = requestData.get("databaseType");

                String customUrl = "jdbc:"+databaseType+"://" + hostname + ":" + port + "/" + databasename;

                ((RelationalJDBCRepository) repository).setUsername(username);
                ((RelationalJDBCRepository) repository).setPassword(password);

                if(testConnection(url, username, password)) ((RelationalJDBCRepository) repository).setUrl(url);
                else if(testConnection(customUrl, username, password)) ((RelationalJDBCRepository) repository).setUrl(customUrl);
            } else if (repository instanceof LocalRepository) {
                ((LocalRepository) repository).setPath(requestData.get("path"));
            } else if (repository instanceof ApiRepository) {
                ((ApiRepository) repository).setUrl(requestData.get("url"));
            } else {
                System.out.println("NO SE HAN PODIDO ASIGNAR LOS PARÁMETROS");
            }
        } else {
            System.out.println("REPOSITORIO NO ENCONTRADO");
        }

        return ormDataResource.save(repository);
    }

    public List<String> getDatabaseTables(String repositoryId) {
        DataRepository repository = ormDataResource.findById(DataRepository.class, repositoryId);

        if (repository != null && repository instanceof RelationalJDBCRepository) {
            return ((RelationalJDBCRepository) repository).retrieveTables();
        } else {
            System.out.println("No se pudo obtener la lista de tablas de la base de datos.");
            return new ArrayList<>(); // Devolver una lista vacía o manejar el error de manera apropiada.
        }
    }

    public List<TableInfo> getDatabaseTablesInfo(String repositoryId) {
        DataRepository repository = ormDataResource.findById(DataRepository.class, repositoryId);

        if (repository != null && repository instanceof RelationalJDBCRepository) {
            return retrieveTablesInfo(((RelationalJDBCRepository) repository).getUrl(), ((RelationalJDBCRepository) repository).getUsername(), ((RelationalJDBCRepository) repository).getPassword());
        } else {
            System.out.println("No se pudo obtener la lista de tablas de la base de datos.");
            return new ArrayList<>(); // Devolver una lista vacía o manejar el error de manera apropiada.
        }
    }

    public List<TableInfo> retrieveTablesInfo(String url, String username, String password) {
        List<TableInfo> tableList = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             Statement statementSize = connection.createStatement();
             Statement statementLines = connection.createStatement();) {

            // Consulta para obtener los nombres de las tablas
            String tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';";
            try (ResultSet resultSet = statement.executeQuery(tableQuery)) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("table_name");
                    System.out.println(tableName + " NOMBRE DE TABLA SQL EN EL REPO POSTGRESQL");

                    // Consulta para obtener el número de filas de la tabla
                    String rowCountQuery = "SELECT COUNT(*) FROM " + tableName + ";";
                    try (ResultSet rowCountResultSet = statementLines.executeQuery(rowCountQuery)) {
                        if (rowCountResultSet.next()) {
                            int tableRowCount = rowCountResultSet.getInt(1);

                            // Cerrar el rowCountResultSet después de obtener el número de filas
                            rowCountResultSet.close();

                            // Consulta para obtener el tamaño de la tabla
                            String tableSizeQuery = "SELECT pg_size_pretty(pg_total_relation_size('" + tableName + "')) AS total_size;";
                            try (ResultSet sizeResultSet = statementSize.executeQuery(tableSizeQuery)) {
                                if (sizeResultSet.next()) {
                                    String tableSize = sizeResultSet.getString("total_size");

                                    sizeResultSet.close();

                                    TableInfo tableInfo = new TableInfo(tableName, tableSize, String.valueOf(tableRowCount));
                                    tableList.add(tableInfo);
                                    System.out.println(tableName + " TABLA AÑADIDA " + tableSize + " " + tableRowCount + " lineas");
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
