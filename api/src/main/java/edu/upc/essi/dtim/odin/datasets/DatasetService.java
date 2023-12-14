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

import edu.upc.essi.dtim.odin.repositories.RepositoryService;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * The service class for managing datasources in a project.
 */
@Service
public class DatasetService {
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
    public DatasetService(@Autowired AppConfig appConfig,
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

    public void postLocalDataset(List<MultipartFile> attachFiles, String datasetDescription, String repositoryId, String projectId) {
        if (attachFiles.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        // Iterate through the list of MultipartFiles to handle each file (the user might have uploaded several files at once)
        for (MultipartFile attachFile : attachFiles) {
            String UUID = generateUUID(); // Unique universal identifier (UUID) of the dataset
            String fullFileName = attachFile.getOriginalFilename(); // Full file name (e.g. directory/filename.extension)
            assert fullFileName != null;

            // We get the file extension, which defines the format of the file. This is because the type of dataset to
            // be created depends on it. If there is no extension, we can not handle the file.
            String format;
            int dotIndex = fullFileName.lastIndexOf('.');
            if (dotIndex == -1) {
                throw new RuntimeException("The files does not have extension and so it can not be handled");
            }
            else {
                format = fullFileName.substring(dotIndex + 1);
            }

            // Get only the file name to add it to the dataset instance (we need it to execute the bootstrap)
            int slashIndex = fullFileName.lastIndexOf("/");
            String datasetName = fullFileName.substring(slashIndex >= 0 ? slashIndex + 1 : 0, dotIndex);

            String newFileName = UUID + "." + format; // New file name using the UUID

            // Reconstruct file from the Multipart file (i.e. store the file in the temporal zone to be accessed later)
            String filePath = storeTemporalFile(attachFile, newFileName);

            // Generate dataset, set UUID parameter and save it (to assign an id)
            Dataset dataset = generateDataset(filePath, datasetName, datasetDescription, repositoryId, "", format);
            dataset.setUUID(UUID);
            dataset = saveDataset(dataset);

            handleDataset(dataset, repositoryId, projectId);
        }
    }

    public void postAPIDataset(List<MultipartFile> attachFiles, String datasetDescription, String repositoryId, String endpoint, String apiDatasetName, String projectId) {
        if (attachFiles.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        for (MultipartFile attachFile : attachFiles) {
            String UUID = generateUUID(); // Unique universal identifier (UUID) of the dataset
            String newFileName = UUID + ".api"; // New file name using the UUID
            // Reconstruct file from the Multipart file (i.e. store the file in the temporal zone to be accessed later)
            String filePath = storeTemporalFile(attachFile, newFileName);
            // Generate dataset, set UUID parameter and save it (to assign an id)
            Dataset dataset = generateDataset(filePath, apiDatasetName, datasetDescription, repositoryId, endpoint, "api");
            dataset.setUUID(UUID);
            dataset = saveDataset(dataset);

            handleDataset(dataset, repositoryId, projectId);
        }
    }


    public void postJDBCDataset(List<String> attachTables, String datasetDescription, String repositoryId, String projectId) {
        for (String tableName : attachTables) {
            // Extract data from datasource file, set UUID parameter and save it (to assign an id)
            String UUID = generateUUID(); // Unique universal identifier (UUID) of the dataset
            Dataset dataset = generateDataset(null, tableName, datasetDescription, repositoryId, null, "sql");
            dataset.setUUID(UUID);
            dataset = saveDataset(dataset);

            handleDataset(dataset, repositoryId, projectId);
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
     * @param repositoryId       Identifier of the repository
     * @param endpoint           The endpoint of the URL used to get the data (when coming from an API).
     * @param format             Format used by the data, which will define the dataset that we create.
     * @return A Dataset object with the extracted data.
     * @throws IllegalArgumentException if the file format is not supported.
     */
    public Dataset generateDataset(String filePath, String datasetName, String datasetDescription, String repositoryId, String endpoint, String format) {
        Dataset dataset;
        DataRepository repository = getRepositoryById(repositoryId);

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

        // Add the dataset to the repository (and save it to generate the id)
        dataset.setRepository(repository);
        dataset = saveDataset(dataset);

        // Add the repository to the dataset
        List<Dataset> newRepoDatasets = new ArrayList<>(repository.getDatasets());
        newRepoDatasets.add(dataset);
        repository.setDatasets(newRepoDatasets);
        repositoryService.saveRepository(repository);

        return dataset;
    }

    private void handleDataset(Dataset dataset, String repositoryId, String projectId) {
        // Execute bootstrap: transform datasource into graph and generate the wrapper
        BootstrapResult bsResult = bootstrapDataset(dataset);
        Graph graph = bsResult.getGraph();
        String wrapper = bsResult.getWrapper();

        // Generating visual schema for frontend
        String visualSchema = generateVisualSchema(graph);
        graph.setGraphicalSchema(visualSchema);

        // Set wrapper to the dataset and add the attributes based on the wrapper
        dataset.setWrapper(wrapper);
        dataset.setAttributes(getAttributesFromWrapper(wrapper));

        // Add the relation between the graph and dataset (this generates an id for the graph)
        Dataset datasetWithGraph = setLocalGraphToDataset(dataset, graph);
        graph.setGraphName(datasetWithGraph.getLocalGraph().getGraphName());

        // If dataset is materialized, store it permanently in the data layer (unless there is an error)
        DataRepository repository = getRepositoryById(repositoryId);
        if (!repository.getVirtual()) {
            boolean error = uploadToDataLayer(datasetWithGraph);
            if (error) {
                deleteDataset(projectId, dataset.getId());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The was an error reading the data. Dataset not created");
            }
        }

        // If there are no errors, we do a final storing of the graph and the dataset
        saveGraphToDatabase(graph);
        saveDataset(datasetWithGraph);
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
        if (projectContains(projectId, datasetId)) {
            // Remove from project
            projectService.deleteDatasetFromProject(projectId, datasetId);
            // Delete rdf file (\jenaFiles)
            try {
                Files.delete(Path.of(appConfig.getJenaPath() + dataset.getLocalGraph().getGraphName() + ".rdf"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Remove from Data layer
            DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
            dlInterface.deleteDataset(dataset.getUUID());
        }
        else {
            throw new RuntimeException("Dataset " + datasetId + " does not belong to project " + projectId);
        }
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
     * Edits a dataset's attributes in the database if any attribute has changed.
     *
     * @return A boolean indicating whether the dataset was edited successfully.
     */
    public boolean editDataset(String datasetId, String datasetName, String datasetDescription) {
        // Get the original dataset in the database to compare the information
        Dataset originalDataset = getDatasetById(datasetId);

        // Check if the attributes have changed
        if (!datasetName.equals(originalDataset.getDatasetName())|| !datasetDescription.equals(originalDataset.getDatasetDescription())) {
            originalDataset.setDatasetName(datasetName);
            originalDataset.setDatasetDescription(datasetDescription);

            saveDataset(originalDataset);
            return true;
        }
        else {
            return false;
        }
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

    public boolean uploadToDataLayer(Dataset dataset) {
        DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
        return dlInterface.uploadToDataLayer(dataset);
    }

    public DataRepository getRepositoryById(String repositoryId) {
       return repositoryService.getRepositoryById(repositoryId);
    }

    public ResponseEntity<InputStreamResource> downloadDatasetSchema(String datasetId) {
        Dataset dataset = getDatasetById(datasetId);

        Model model = dataset.getLocalGraph().getGraph(); // Get the RDF model (graph) from the dataset
        StringWriter writer = new StringWriter();

        model.write(writer, "TTL"); // Write the model (graph) to a StringWriter in Turtle format

        HttpHeaders headers = new HttpHeaders();
        // Set the HTTP headers to specify the content disposition as an attachment with the dataset name and .ttl extension
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + dataset.getDatasetName() + ".ttl");

        // Create an InputStreamResource from the StringWriter
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(writer.toString().getBytes()));
        // Return a ResponseEntity with the Turtle schema file, content type, and headers
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/turtle"))
                .body(resource);
    }
}


