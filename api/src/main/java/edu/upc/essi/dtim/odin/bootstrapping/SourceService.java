package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.ApiRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMDuckDB;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DLMSpark;
import edu.upc.essi.dtim.NextiaDataLayer.materialized.DataLayerMaterialized;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS.bsModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS.bsModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterace;
import edu.upc.essi.dtim.odin.project.Project;
import edu.upc.essi.dtim.odin.project.ProjectService;
import edu.upc.essi.dtim.odin.repositories.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * The service class for managing datasources in a project.
 */
@Service
public class SourceService {
    /**
     * The dependency on the ProjectService class.
     */
    private final ProjectService projectService;
    private final RepositoryService repositoryService;
    /**
     * The AppConfig dependency for accessing application configuration.
     */
    private final AppConfig appConfig;
    /**
     * The ORMStoreInterface dependency for storing datasets.
     */
    private final ORMStoreInterface ormDataResource;

    /**
     * Constructs a new instance of SourceService.
     *
     * @param appConfig      The AppConfig dependency for accessing application configuration.
     * @param projectService The ProjectService dependency.
     */
    public SourceService(@Autowired AppConfig appConfig,
                         @Autowired ProjectService projectService,
                         @Autowired RepositoryService repositoryService) {
        this.appConfig = appConfig;
        this.projectService = projectService;
        this.repositoryService = repositoryService;
        try {
            this.ormDataResource = ORMStoreFactory.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores a multipart file in the specified disk path with a modified filename and returns the absolute path of the file.
     *
     * @param multipartFile The multipart file to store.
     * @return The absolute path of the stored file.
     * @throws RuntimeException if the file is empty or an error occurs during the file storage process.
     */
    public String reconstructFile(MultipartFile multipartFile, String repositoryIdAndName) {
        try {
            if (multipartFile.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            String originalFilename = multipartFile.getOriginalFilename();

            if (originalFilename != null) {
                int lastSlashIndex = originalFilename.lastIndexOf("/");

                if (lastSlashIndex >= 0) {
                    originalFilename = originalFilename.substring(lastSlashIndex + 1);
                    // extractedSubstring ahora contiene la parte de la cadena después de la última "/"
                    System.out.println("Substring extraída: " + originalFilename);
                } else {
                    // No se encontró "/" en el nombre de archivo original, por lo que originalFilename no se modifica.
                    System.out.println("No se encontró '/' en el nombre de archivo original.");
                }
            }

            String modifiedFilename = repositoryIdAndName + "/" + originalFilename;

            System.out.println(originalFilename);
            System.out.println(modifiedFilename);

            // Get the disk path from the app configuration
            Path diskPath = Path.of(appConfig.getDiskPath());

            // Resolve the destination file path using the disk path and the modified filename
            Path destinationFile = diskPath.resolve(Paths.get(modifiedFilename));

            // Create parent directories if they don't exist
            Files.createDirectories(destinationFile.getParent());

            // Copy the input stream of the multipart file to the destination file
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return the absolute path of the stored file
            return destinationFile.toString();
        } catch (IOException e) {
            // Throw a runtime exception if an error occurs during file storage
            throw new RuntimeException("Failed to store file.", e);
        }
    }



    /**
     * Extracts data from a file and returns a Dataset object with the extracted data.
     *
     * @param filePath           The path of the file to extract data from.
     * @param datasetName        The name of the dataset.
     * @param datasetDescription The description of the dataset.
     * @return A Dataset object with the extracted data.
     * @throws IllegalArgumentException if the file format is not supported.
     */
    public Dataset extractData(String filePath, String datasetName, String datasetDescription) throws SQLException, IOException, ClassNotFoundException {
        if(filePath == null) filePath = "table.sql";
        // Extract the extension of the file from the file path
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1);

        Dataset dataset;

        // Create a new dataset object with the extracted data based on the file extension
        switch (extension.toLowerCase()) {
            case "csv":
                // Create a CsvDataset object for CSV files
                dataset = new CsvDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "json":
                // Create a JsonDataset object for JSON files
                dataset = new JsonDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "sql":
                // Create a SqlDataset object for JSON files
                dataset = new SQLDataset(null, datasetName, datasetDescription, datasetName, "dtim.essi.upc.edu", "5432", "vasenjo", "jBGRfEu");
                break;
            case "xml":
                // Create a XmlDataset object for JSON files
                dataset = new XmlDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "parquet":
                // Create a ParquetDataset object for JSON files
                dataset = new ParquetDataset(null, datasetName, datasetDescription, filePath);
                break;
            default:
                // Throw an exception for unsupported file formats
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }

        // Assign an ID to the dataset
        dataset = saveDataset(dataset);
        String id = dataset.getId();
        String datasetId = id;

        // Modify the datasetPath to include the ID
        String datasetPath;
        int lastSlashIndex = Math.max(filePath.lastIndexOf("/"), filePath.lastIndexOf("\\"));
        if (lastSlashIndex >= 0) {
            datasetPath = filePath.substring(0, lastSlashIndex + 1) + datasetId + filePath.substring(lastSlashIndex + 1);
            System.out.println(datasetPath + "+++++++++++++++++++ dataset path slash detection");
            System.out.println(filePath + "+++++++++++++++++++ FILE ORIGINAL path slash detection");

        } else {
            datasetPath = datasetId + filePath;
            System.out.println(datasetPath + "+++++++++++++++++++ dataset path SIN SLASH");
        }


        // Update the datasetPath in the dataset object
        if (dataset instanceof JsonDataset) {
            ((JsonDataset) dataset).setPath(datasetPath);
        } else if (dataset instanceof CsvDataset) {
            ((CsvDataset) dataset).setPath(datasetPath);
        } else if (dataset instanceof XmlDataset) {
            ((XmlDataset) dataset).setPath(datasetPath);
        } else if (dataset instanceof ParquetDataset) {
            ((ParquetDataset) dataset).setPath(datasetPath);
        }

        // Rename the file on disk with the updated datasetPath
        File originalFile = new File(filePath);
        File renamedFile = new File(datasetPath);
        if (originalFile.renameTo(renamedFile)) {
            System.out.println("File renamed successfully.");
        } else {
            System.out.println("File renaming failed.");
        }

        // Save the dataset again with the updated datasetPath
        dataset = saveDataset(dataset);
        System.out.println(id);

        //TODO IMPORT NextiaDatalayer
        //TODO delete if when NextiaDatalayer accepts SQL and other dataset formats
        if(dataset instanceof CsvDataset || dataset instanceof JsonDataset) {
            DataLayerInterace dlInterface = new DataLayerImpl(appConfig);
            dataset.setDataLayerPath(generateUUID());
            dlInterface.uploadToDataLayer(dataset);
        }

        dataset = saveDataset(dataset);

        return dataset;
    }

    private String generateUUID() {
        // Generate a random 16-character string as part of the filename
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(16);
        SecureRandom random = new SecureRandom();
        sb.append("PREFIX");
        for (int i = 0; i < 16; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }

        return sb.toString();
    }

    /**
     * Transforms a Dataset object into a Graph object representing the data in RDF format.
     *
     * @param dataset The Dataset object to transform.
     * @return A GraphModelPair object containing the transformed Graph and the corresponding Model.
     * @throws UnsupportedOperationException if the dataset type is not supported or an error occurs during the transformation.
     */
    public BootstrapResult bootstrapDataset(Dataset dataset) {
        try {
            // Create an instance of the bsModuleImpl class that implements the bsModuleInterface
            bsModuleInterface bsInterface = new bsModuleImpl();

            // Use the bsInterface to convert the dataset to a Graph object
            return bsInterface.bootstrapDataset(dataset);
        } catch (UnsupportedOperationException e) {
            // Throw an exception if the dataset type is not supported or an error occurs during the transformation
            throw new UnsupportedOperationException("Dataset type not supported. Something went wrong during the bootstrap process generating the schema.");
        }
    }

    public Graph bootstrapDatasetG(Dataset dataset) {
        try {
            // Create an instance of the bsModuleImpl class that implements the bsModuleInterface
            bsModuleInterface bsInterface = new bsModuleImpl();

            // Use the bsInterface to convert the dataset to a Graph object
            return bsInterface.bootstrapGraph(dataset);
        } catch (UnsupportedOperationException e) {
            // Throw an exception if the dataset type is not supported or an error occurs during the transformation
            throw new UnsupportedOperationException("Dataset type not supported. Something went wrong during the bootstrap process generating the schema.");
        }
    }

    /**
     * Generates a visual representation of a Graph using the NextiaGraphy library.
     *
     * @param graph The GraphModelPair object containing the Graph.
     * @return A String representing the visual schema of the Graph.
     */
    public String generateVisualSchema(Graph graph) {
        // Create an instance of the nextiaGraphyModuleImpl class that implements the nextiaGraphyModuleInterface
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();

        // Use the visualLibInterface to generate a visual representation of the Graph
        return visualLibInterface.generateVisualGraph(graph);
    }

    /**
     * Saves a Graph object to the database using a GraphStoreInterface.
     *
     * @param graph The Graph object to save.
     * @return A boolean indicating whether the saving operation was successful.
     * @throws RuntimeException if an error occurs during the graph storage process.
     */
    public boolean saveGraphToDatabase(Graph graph) {
        GraphStoreInterface graphStore;
        try {
            // Get an instance of the GraphStoreInterface using the appConfig
            graphStore = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            // Throw a runtime exception if an error occurs while getting the GraphStoreInterface
            throw new RuntimeException(e);
        }

        // Ensure that the graphStore is not null
        assert graphStore != null;

        // Save the Graph object to the database using the graphStore
        graphStore.saveGraph(graph);

        // Return true to indicate a successful saving operation
        return true;
    }

    /**
     * Deletes a dataset from a project using the ProjectService class.
     *
     * @param projectId The ID of the project to delete the dataset from.
     * @param datasetId The ID of the dataset to delete.
     */
    public void deleteDatasetFromProject(String projectId, String datasetId) {
        // Call the projectService to delete the dataset from the specified project
        projectService.deleteDatasetFromProject(projectId, datasetId);
    }

    /**
     * Saves a Dataset object using the ORMStoreInterface.
     *
     * @param dataset The Dataset object to save.
     * @return The saved Dataset object.
     */
    public Dataset saveDataset(Dataset dataset) {
        // Save the Dataset object using the ORMStoreInterface and return the saved Dataset object
        return ormDataResource.save(dataset);
    }

    /**
     * Retrieves all datasets from the ORMStoreInterface.
     *
     * @return A list of Dataset objects.
     */
    public List<Dataset> getDatasets() {
        // Retrieve all Dataset objects from the ORMStoreInterface and return them as a list
        return ormDataResource.getAll(Dataset.class);
    }

    /**
     * Deletes a datasource from the ORMStoreInterface. NOT USED. When deleteDatasetFromProject(...) cascade all does this implicit.
     *
     * @param id The ID of the datasource to delete.
     * @return A boolean indicating whether the deletion was successful.
     */
    public boolean deleteDatasource(String id) {
        // Delete a datasource with the specified ID from the ORMStoreInterface (not used)
        return ormDataResource.deleteOne(Dataset.class, id);
    }

    /**
     * Checks if a project contains a specific dataset using the ProjectService class.
     *
     * @param projectId The ID of the project to check.
     * @param id        The ID of the dataset to check.
     * @return A boolean indicating whether the project contains the dataset.
     */
    public boolean projectContains(String projectId, String id) {
        // Use the ProjectService to check if the specified project contains the dataset with the given ID
        return projectService.projectContains(projectId, id);
    }

    /**
     * Retrieves all datasets of a project using the ProjectService class.
     *
     * @param id The ID of the project to retrieve datasets from.
     * @return A list of Dataset objects associated with the project.
     */
    public List<Dataset> getDatasetsOfProject(String id) {
        // Use the ProjectService to retrieve a list of Dataset objects associated with the specified project ID
        return projectService.getDatasetsOfProject(id);
    }

    /**
     * Sets a local graph to a Dataset and saves it.
     *
     * @param savedDataset The Dataset object to which the local graph will be set.
     * @param graph        The Graph object containing the local graph data.
     * @return The updated Dataset object with the local graph set.
     */
    public Dataset setLocalGraphToDataset(Dataset savedDataset, Graph graph) {
        // Create a new LocalGraphJenaImpl instance
        LocalGraphJenaImpl localGraph = CoreGraphFactory.createLocalGraph();

        // Set the graph name and graphical schema from the provided Graph object
        localGraph.setGraphName(graph.getGraphName());
        localGraph.setGraphicalSchema(graph.getGraphicalSchema());

        // Set the local graph to the saved Dataset object
        savedDataset.setLocalGraph(localGraph);

        // Save the updated Dataset and return it
        return (Dataset) saveDataset(savedDataset);
    }

    /**
     * Finds a DataRepository by its ID using the ORMStoreInterface.
     *
     * @param repositoryId The ID of the DataRepository to find.
     * @return The found DataRepository or null if not found.
     */
    public DataRepository findRepositoryById(String repositoryId) {
        // Find and return a DataRepository with the specified ID using the ORMStoreInterface
        return ormDataResource.findById(DataRepository.class, repositoryId);
    }


    /**
     * Creates a new DataRepository with the specified repository name using the ORMStoreInterface.
     *
     * @param repositoryName The name of the DataRepository to create.
     * @return The created DataRepository.
     */
    public DataRepository createRepository(String repositoryName, String repositoryType) {
        return repositoryService.createRepository(repositoryName, repositoryType);
    }


    /**
     * Adds a dataset to a DataRepository and updates the repository associations using the ORMStoreInterface.
     *
     * @param datasetId    The ID of the dataset to add to the repository.
     * @param repositoryId The ID of the repository to which the dataset will be added.
     * @return The updated DataRepository containing the added dataset.
     * @throws IllegalArgumentException If the repository or dataset is not found.
     */
    public DataRepository addDatasetToRepository(String datasetId, String repositoryId) throws IllegalArgumentException {
        // Find the new repository and dataset by their respective IDs
        DataRepository newRepository = findRepositoryById(repositoryId);
        Dataset dataset = getDatasetById(datasetId);

        // Check if the new repository and dataset were found
        if (newRepository == null) {
            throw new IllegalArgumentException("Repository not found with repositoryId: " + repositoryId);
        }

        if (dataset == null) {
            throw new IllegalArgumentException("Dataset not found with datasetId: " + datasetId);
        }

        if (datasetIsTypeOfRepositoryRestriction(datasetId, repositoryId)){
            // Remove the dataset from the old repository if it exists
            DataRepository oldRepository = findRepositoryContainingDataset(datasetId);
            if (oldRepository != null) {
                List<Dataset> oldRepoDatasets = new ArrayList<>(oldRepository.getDatasets());
                oldRepoDatasets.removeIf(d -> d.getId().equals(datasetId));
                oldRepository.setDatasets(oldRepoDatasets);
                ormDataResource.save(oldRepository);
            }

            // Add the dataset to the new repository
            List<Dataset> newRepoDatasets = new ArrayList<>(newRepository.getDatasets());

            newRepoDatasets.add(dataset);

            newRepository.setDatasets(newRepoDatasets);

            // Save both repositories
            DataRepository savedRepository = ormDataResource.save(newRepository);

            return savedRepository;
        }
        return null;
    }


    /**
     * Finds a DataRepository that contains a specific dataset using the ORMStoreInterface.
     *
     * @param datasetId The ID of the dataset to search for in the repositories.
     * @return The DataRepository containing the specified dataset, or null if not found.
     */
    private DataRepository findRepositoryContainingDataset(String datasetId) {
        // Retrieve a list of all DataRepositories
        List<DataRepository> repositories = ormDataResource.getAll(DataRepository.class);

        // Iterate through the repositories
        for (DataRepository repository : repositories) {
            List<Dataset> datasets = repository.getDatasets();

            // Iterate through the datasets in each repository
            for (Dataset dataset : datasets) {
                // Check if the dataset ID matches the specified datasetId
                if (dataset.getId().equals(datasetId)) {
                    return repository; // Found the repository containing the dataset
                }
            }
        }

        return null; // Dataset not found in any repository
    }


    /**
     * Adds a DataRepository to a specific project using the ORMStoreInterface and ProjectService.
     *
     * @param projectId    The ID of the project to which the repository will be added.
     * @param repositoryId The ID of the DataRepository to be added to the project.
     */
    public void addRepositoryToProject(String projectId, String repositoryId) {
        repositoryService.addRepositoryToProject(projectId, repositoryId);
    }


    /**
     * Retrieves all DataRepositories associated with a specific project using the ProjectService.
     *
     * @param id The ID of the project to retrieve repositories from.
     * @return A list of DataRepository objects associated with the project.
     */
    public List<DataRepository> getRepositoriesOfProject(String id) {
        // Call the ProjectService to retrieve all DataRepositories associated with the specified project
        return projectService.getRepositoriesOfProject(id);
    }


    /**
     * Edits a dataset's attributes in the database if any attribute has changed.
     *
     * @param dataset The updated Dataset object to be saved.
     * @return A boolean indicating whether the dataset was edited successfully.
     */
    public boolean editDataset(Dataset dataset) {
        // Retrieve the original dataset from the database using its ID
        Dataset originalDataset = getDatasetById(dataset.getId());
        Dataset savedDS = null;

        // Check if any attribute has changed
        if (!dataset.getDatasetName().equals(originalDataset.getDatasetName())
                || !dataset.getDatasetDescription().equals(originalDataset.getDatasetDescription())) {
            // At least one attribute has changed

            // Update the attributes of the original dataset with the new values
            originalDataset.setDatasetName(dataset.getDatasetName());
            originalDataset.setDatasetDescription(dataset.getDatasetDescription());

            // Perform the database update operation to save the changes
            savedDS = saveDataset(originalDataset);
        }

        if(savedDS.getId() != null) return true;

        // No changes detected, return false
        return false;
    }


    /**
     * Deletes a dataset from the list of datasets in a repository based on the dataset's ID.
     *
     * @param projectId The ID of the project to find the repository.
     * @param datasetId The ID of the dataset to delete.
     */
    public void deleteDatasetFromRepo(String projectId, String datasetId) {
        // Retrieve the project by its ID
        Project project = projectService.getProjectById(projectId);

        // Check if the project exists
        if (project == null) {
            throw new IllegalArgumentException("Project not found with projectId: " + projectId);
        }

        DataRepository targetRepository = null;

        // Iterate through the repositories in the project
        for (DataRepository repository : project.getRepositories()) {
            // Iterate through the datasets in each repository
            for (Dataset dataset : repository.getDatasets()) {
                // Check if the dataset's ID matches the provided datasetId
                if (dataset.getId().equals(datasetId)) {
                    targetRepository = repository; // Store the repository containing the dataset
                    break;
                }
            }
            if (targetRepository != null) {
                break; // Exit the loop once the target repository is found
            }
        }

        if (targetRepository != null) {
            // Remove the dataset from the target repository's list of datasets
            List<Dataset> datasets = new ArrayList<>(targetRepository.getDatasets());
            datasets.removeIf(dataset -> dataset.getId().equals(datasetId));
            targetRepository.setDatasets(datasets);

            // Save the updated repository to the database
            ormDataResource.save(targetRepository);
        }
    }


    /**
     * Checks if a project has an integrated graph assigned.
     *
     * @param projectId The ID of the project to check.
     * @return A boolean indicating whether the project has an integrated graph assigned.
     */
    public boolean projectHasIntegratedGraph(String projectId) {
        // Retrieve the project with the given ID
        Project project = projectService.getProjectById(projectId);

        // If the project is not found, throw an exception
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        // Check if the project has an integrated graph assigned
        return project.getIntegratedGraph() != null;
    }


    /**
     * Assigns the schema of a dataset to the integrated graph of a project.
     *
     * @param projectId The ID of the project to which the schema is assigned.
     * @param datasetId The ID of the dataset whose schema is assigned to the project.
     */
    public void setProjectSchemasBase(String projectId, String datasetId) {
        // Retrieve the project with the given ID
        Project project = projectService.getProjectById(projectId);

        // If the project is not found, throw an exception
        if (project == null) {
            throw new IllegalArgumentException("Project not found");
        }

        // Find the dataset with the given datasetId
        Dataset dataset = getDatasetById(datasetId);

        // If the dataset is not found in the project, throw an exception
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset not found in the project");
        }

        // Assign the schema of the dataset to the project's integrated graph
        try {
            Graph integratedGraph = CoreGraphFactory.createIntegratedGraph();
            GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);

            Graph datasetGraph = graphStore.getGraph(dataset.getLocalGraph().getGraphName());

            integratedGraph.setGraphName(null);
            integratedGraph.setGraph(datasetGraph.getGraph());
            integratedGraph.setGraphicalSchema(datasetGraph.getGraphicalSchema());

            project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
            projectService.saveProject(project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Retrieves a dataset by its unique identifier and returns it with its associated graph.
     *
     * @param datasetId The unique identifier of the dataset to retrieve.
     * @return The dataset object with its associated graph.
     */
    public Dataset getDatasetById(String datasetId) {
        // Retrieve the dataset by its unique identifier
        Dataset dataset = ormDataResource.findById(Dataset.class, datasetId);

        // Retrieve the content of the graph associated with the dataset
        GraphStoreInterface graphStore;
        try {
            graphStore = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assert graphStore != null;

        Graph datasetGraph = null;
        if (dataset.getLocalGraph() != null && dataset.getLocalGraph().getGraphName() != null) datasetGraph = graphStore.getGraph(dataset.getLocalGraph().getGraphName());

        // Set the local graph of the dataset to the retrieved graph
        dataset.setLocalGraph((LocalGraphJenaImpl) datasetGraph);

        return dataset;
    }

    public Project addIntegratedDataset(String projectID, String datasetID) {
        return projectService.addIntegratedDataset(projectID, datasetID);
    }

    public Project deleteIntegratedDatasets(String projectID) {
        return projectService.deleteIntegratedDatasets(projectID);
    }

    public Dataset addRepositoryToDataset(String datasetId, String repositoryId) {
        // Find the new repository and dataset by their respective IDs
        DataRepository newRepository = findRepositoryById(repositoryId);
        Dataset dataset = getDatasetById(datasetId);

        // Check if the new repository and dataset were found
        if (newRepository == null) {
            throw new IllegalArgumentException("Repository not found with repositoryId: " + repositoryId);
        }

        if (dataset == null) {
            throw new IllegalArgumentException("Dataset not found with datasetId: " + datasetId);
        }

        if(datasetIsTypeOfRepositoryRestriction(datasetId, repositoryId)){
            dataset.setRepository(newRepository);

            // Save both repositories
            Dataset savedDataset = saveDataset(dataset);

            return savedDataset;
        }
        return null;
    }

    private boolean datasetIsTypeOfRepositoryRestriction(String datasetId, String repositoryId) {
        // Find the new repository and dataset by their respective IDs
        DataRepository repository = findRepositoryById(repositoryId);
        Dataset dataset = getDatasetById(datasetId);

        // Check if the new repository and dataset were found
        if (repository == null) {
            throw new IllegalArgumentException("Repository not found with repositoryId: " + repositoryId);
        }

        if (dataset == null) {
            throw new IllegalArgumentException("Dataset not found with datasetId: " + datasetId);
        }

        // Check if the dataset type matches the repository type
        if (repository instanceof RelationalJDBCRepository && dataset instanceof SQLDataset) {
            return true; // DatasetSQL can be added to RepositorySQL
        } else if (repository instanceof LocalRepository &&
                        (dataset instanceof JsonDataset ||
                        dataset instanceof CsvDataset ||
                        dataset instanceof ParquetDataset ||
                        dataset instanceof XmlDataset)) {
            return true; // LocalDataset can be added to LocalRepository
        } else if (repository instanceof ApiRepository &&
                (dataset instanceof JsonDataset ||
                        dataset instanceof CsvDataset ||
                        dataset instanceof ParquetDataset ||
                        dataset instanceof XmlDataset)) {
            return true; // LocalDataset can be added to ApiRepository
        } else {
            return false; // Incompatible dataset and repository types
        }
    }

    public Dataset setWrapperToDataset(String datasetId, String wrapper) {
        Dataset dataset = getDatasetById(datasetId);
        dataset.setWrapper(wrapper);

        // Save the updated Dataset and return it
        return saveDataset(dataset);
    }
}


