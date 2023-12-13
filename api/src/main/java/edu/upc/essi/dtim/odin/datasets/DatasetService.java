package edu.upc.essi.dtim.odin.datasets;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.ApiRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.LocalRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
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
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import edu.upc.essi.dtim.odin.project.Project;
import edu.upc.essi.dtim.odin.project.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * The service class for managing datasources in a project.
 */
@Service
public class DatasetService {
    private final ProjectService projectService;
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
    public DatasetService(@Autowired AppConfig appConfig,
                          @Autowired ProjectService projectService) {
        this.appConfig = appConfig;
        this.projectService = projectService;
        try {
            this.ormDataResource = ORMStoreFactory.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores a multipart file in the specified disk path with a modified filename and returns the absolute path of the file.
     *
     * @param multipartFile     The multipart file to store.
     * @param newFileDirectory  Directory of the new file
     * @return                  The absolute path of the stored file.
     * @throws RuntimeException If the file is empty or an error occurs during the file storage process.
     */
    public String storeTemporalFile(MultipartFile multipartFile, String newFileDirectory) {
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        return dataLayerInterFace.storeTemporalFile(multipartFile, newFileDirectory);
    }

    /**
     * Extracts data from a file and returns a Dataset object with the extracted data.
     *
     * @param filePath           The path of the file to extract data from.
     * @param datasetName        The name of the dataset.
     * @param datasetDescription The description of the dataset.
     * @param repository         The repository the dataset belongs to.
     * @param endpoint           The endpoint of the URL used to get the data (when coming from an API).
     * @param format             Format used by the data, which will define the dataset that we create.
     * @return A Dataset object with the extracted data.
     * @throws IllegalArgumentException if the file format is not supported.
     */
    public Dataset generateDataset(String filePath, String datasetName, String datasetDescription, DataRepository repository, String endpoint, String format) {
        Dataset dataset;

        switch (format) { // Create a new dataset object with the extracted data based on the format
            case "csv":
                dataset = new CsvDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "json":
                dataset = new JsonDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "api":
                dataset = new APIDataset(null, datasetName, datasetDescription, endpoint, filePath);
                break;
            case "sql":
                String password = ((RelationalJDBCRepository) repository).getPassword();
                String username = ((RelationalJDBCRepository) repository).getUsername();
                dataset = new SQLDataset(null, datasetName, datasetDescription, datasetName, ((RelationalJDBCRepository) repository).retrieveHostname(), ((RelationalJDBCRepository) repository).retrievePort(), username, password);
                break;
            case "xml":
                dataset = new XmlDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "parquet":
                dataset = new ParquetDataset(null, datasetName, datasetDescription, filePath);
                break;
            default: // Throw an exception for unsupported file formats
                throw new IllegalArgumentException("Unsupported file format: " + format);
        }

        dataset = saveDataset(dataset);
        return dataset;
    }

    /**
     * Extracts the attributes of the dataset from the wrapper. This needs to be done, as the wrapper dictates which
     * are the attributes of the dataset.
     *
     * @param wrapper wrapper obtained from the bootstrapping process of the dataset
     * @return A Dataset object with the extracted data.
     * @throws IllegalArgumentException if the file format is not supported.
     */
    public List<Attribute> getAttributesFromWrapper(String wrapper) {
        List<Attribute> attributes = new ArrayList<>();
        int backtickIndex = wrapper.indexOf("`");
        while (backtickIndex != -1) {
            int nextBacktickIndex = wrapper.indexOf("`", backtickIndex + 1);
            attributes.add(generateAttribute(wrapper.substring(backtickIndex + 1, nextBacktickIndex)));
            backtickIndex = wrapper.indexOf("`", nextBacktickIndex + 1);
        }
        return attributes;
    }

    private Attribute generateAttribute(String attributeName) {
        return new Attribute(attributeName, "string");
    }

    /**
     * Generates a universal unique identifier for the dataset
     */
    public String generateUUID() {
        // Generate a random 16-character string as part of the filename
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(16);
        SecureRandom random = new SecureRandom();
        sb.append("UUID_");
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
            bsModuleInterface bsInterface = new bsModuleImpl();
            return bsInterface.bootstrapDataset(dataset);
        } catch (UnsupportedOperationException e) {
            throw new UnsupportedOperationException("Dataset type not supported. Something went wrong during the bootstrap process generating the schema.");
        }
    }

    public Graph bootstrapDatasetG(Dataset dataset) {
        try {
            bsModuleInterface bsInterface = new bsModuleImpl();
            return bsInterface.bootstrapGraph(dataset);
        } catch (UnsupportedOperationException e) {
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
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        return visualLibInterface.generateVisualGraph(graph);
    }

    /**
     * Saves a Graph object to the database using a GraphStoreInterface.
     *
     * @param graph The Graph object to save.
     * @throws RuntimeException if an error occurs during the graph storage process.
     */
    public void saveGraphToDatabase(Graph graph) {
        GraphStoreInterface graphStore;
        try {
            graphStore = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assert graphStore != null;
        graphStore.saveGraph(graph); // Save the Graph object to the database
    }

    /**
     * Deletes a dataset from a project using the ProjectService class.
     *
     * @param projectId The ID of the project to delete the dataset from.
     * @param datasetId The ID of the dataset to delete.
     */
    public void deleteDataset(String projectId, String datasetId) {
        Dataset dataset = getDatasetById(datasetId);
        // Remove from Data layer
        DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
        dlInterface.deleteDataset(dataset.getUUID());
        // Remove from project and remove the rdf file
        projectService.deleteDatasetFromProject(projectId, datasetId);
    }

    /**
     * Saves a Dataset object using the ORMStoreInterface.
     *
     * @param dataset The Dataset object to save.
     * @return The saved Dataset object.
     */
    public Dataset saveDataset(Dataset dataset) {
        return ormDataResource.save(dataset);
    }

    /**
     * Retrieves all datasets from the ORMStoreInterface.
     *
     * @return A list of Dataset objects.
     */
    public List<Dataset> getDatasets() {
        return ormDataResource.getAll(Dataset.class);
    }

    /**
     * Checks if a project contains a specific dataset using the ProjectService class.
     *
     * @param projectId The ID of the project to check.
     * @param id        The ID of the dataset to check.
     * @return A boolean indicating whether the project contains the dataset.
     */
    public boolean projectContains(String projectId, String id) {
        return projectService.projectContains(projectId, id);
    }

    /**
     * Retrieves all datasets of a project using the ProjectService class.
     *
     * @param id The ID of the project to retrieve datasets from.
     * @return A list of Dataset objects associated with the project.
     */
    public List<Dataset> getDatasetsOfProject(String id) {
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
        LocalGraphJenaImpl localGraph = CoreGraphFactory.createLocalGraph();

        // Set the graph name and graphical schema from the provided Graph object
        localGraph.setGraphName(graph.getGraphName());
        localGraph.setGraphicalSchema(graph.getGraphicalSchema());

        // Set the local graph to the saved Dataset object
        savedDataset.setLocalGraph(localGraph);

        // Save the updated Dataset and return it
        return saveDataset(savedDataset);
    }

    /**
     * Finds a DataRepository by its ID using the ORMStoreInterface.
     *
     * @param repositoryId The ID of the DataRepository to find.
     * @return The found DataRepository or null if not found.
     */
    public DataRepository findRepositoryById(String repositoryId) {
        DataRepository repo = ormDataResource.findById(DataRepository.class, repositoryId);
        if (repo == null) {
            throw new IllegalArgumentException("Repository not found with repositoryId: " + repositoryId);
        }
        return repo;
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
        DataRepository newRepository = findRepositoryById(repositoryId);
        Dataset dataset = getDatasetById(datasetId);

        if (datasetIsTypeOfRepositoryRestriction(datasetId, repositoryId)) {
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
            return ormDataResource.save(newRepository);
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

        for (DataRepository repository : repositories) { // Iterate through the repositories
            List<Dataset> datasets = repository.getDatasets();

            for (Dataset dataset : datasets) { // Iterate through the datasets in each repository
                // Check if the dataset ID matches the specified datasetId
                if (dataset.getId().equals(datasetId)) {
                    return repository; // Found the repository containing the dataset
                }
            }
        }

        return null; // Dataset not found in any repository
    }

    /**
     * Retrieves all DataRepositories associated with a specific project using the ProjectService.
     *
     * @param id The ID of the project to retrieve repositories from.
     * @return A list of DataRepository objects associated with the project.
     */
    public List<DataRepository> getRepositoriesOfProject(String id) {
        return projectService.getRepositoriesOfProject(id);
    }

    /**
     * Edits a dataset's attributes in the database if any attribute has changed.
     *
     * @param dataset The updated Dataset object to be saved.
     * @return A boolean indicating whether the dataset was edited successfully.
     */
    public boolean editDataset(Dataset dataset) {
        Dataset originalDataset = getDatasetById(dataset.getId());
        Dataset savedDS = null;

        // Check if any attribute has changed
        if (!dataset.getDatasetName().equals(originalDataset.getDatasetName())
                || !dataset.getDatasetDescription().equals(originalDataset.getDatasetDescription())) {

            // Update the attributes of the original dataset with the new values
            originalDataset.setDatasetName(dataset.getDatasetName());
            originalDataset.setDatasetDescription(dataset.getDatasetDescription());

            // Perform the database update operation to save the changes
            savedDS = saveDataset(originalDataset);
        }

        // return true if some change had been made, return false otherwise
        return savedDS != null;
    }

    /**
     * Checks if a project has an integrated graph assigned.
     *
     * @param projectId The ID of the project to check.
     * @return A boolean indicating whether the project has an integrated graph assigned.
     */
    public boolean projectHasIntegratedGraph(String projectId) {
        Project project = projectService.getProjectById(projectId);
        return project.getIntegratedGraph() != null;
    }

    /**
     * Assigns the schema of a dataset to the integrated graph of a project.
     *
     * @param projectId The ID of the project to which the schema is assigned.
     * @param datasetId The ID of the dataset whose schema is assigned to the project.
     */
    public void setProjectSchemasBase(String projectId, String datasetId) {
        Project project = projectService.getProjectById(projectId);
        Dataset dataset = getDatasetById(datasetId);

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
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset not found with datasetId: " + datasetId);
        }

        // Retrieve the content of the graph associated with the dataset
        GraphStoreInterface graphStore;
        try {
            graphStore = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assert graphStore != null;

        Graph datasetGraph = null;
        if (dataset.getLocalGraph() != null && dataset.getLocalGraph().getGraphName() != null)
            datasetGraph = graphStore.getGraph(dataset.getLocalGraph().getGraphName());

        // Set the local graph of the dataset to the retrieved graph
        dataset.setLocalGraph((LocalGraphJenaImpl) datasetGraph);

        return dataset;
    }

    public void addIntegratedDataset(String projectID, String datasetID) {
        projectService.addIntegratedDataset(projectID, datasetID);
    }

    public void deleteIntegratedDatasets(String projectID) {
        projectService.deleteIntegratedDatasets(projectID);
    }

    public Dataset addRepositoryToDataset(String datasetId, String repositoryId) {
        DataRepository newRepository = findRepositoryById(repositoryId);
        Dataset dataset = getDatasetById(datasetId);

        if (datasetIsTypeOfRepositoryRestriction(datasetId, repositoryId)) {
            dataset.setRepository(newRepository);

            // Save both repositories
            return saveDataset(dataset);
        }
        return null;
    }

    private boolean datasetIsTypeOfRepositoryRestriction(String datasetId, String repositoryId) {
        DataRepository repository = findRepositoryById(repositoryId);
        Dataset dataset = getDatasetById(datasetId);

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
                        dataset instanceof XmlDataset ||
                        dataset instanceof APIDataset)) {
            return true; // LocalDataset can be added to ApiRepository
        } else {
            return false; // Incompatible dataset and repository types
        }
    }

    public boolean uploadToDataLayer(Dataset dataset) {
        DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
        return dlInterface.uploadToDataLayer(dataset);
    }
}


