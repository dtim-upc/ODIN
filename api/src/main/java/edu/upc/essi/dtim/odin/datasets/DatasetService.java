package edu.upc.essi.dtim.odin.datasets;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.exception.EmptyFileException;
import edu.upc.essi.dtim.odin.exception.FormatNotAcceptedException;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
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
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.projects.pojo.Project;

import edu.upc.essi.dtim.odin.repositories.RepositoryService;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static edu.upc.essi.dtim.odin.utils.Utils.generateUUID;

@Service
public class DatasetService {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RestTemplate restTemplate;
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();

    /**
     * Indicates the function to execute the generation of a new dataset based on the type of repository the dataset belongs to.
     *
     * @param projectID          Identification of the project to which the new dataset will be added.
     * @param repositoryId       Identification of the repository to which the new dataset will belong to.
     * @param attachFiles        The attached files representing the datasets.
     * @param attachTables       The attached tables representing the datasets.
     * @param endpoint           Endpoint of the URL to get the data from
     * @param apiDatasetName     Name given to the dataset by the user in the frontend
     * @param datasetDescription The description of the dataset.
     */
    public void postDataset(String projectID, String repositoryId, List<MultipartFile> attachFiles, List<String> attachTables, String apiDatasetName, String endpoint, String datasetDescription) {
        DataRepository repository = repositoryService.getRepositoryById(repositoryId);
        switch (repository.getRepositoryType()) { // Depending on the type of repo, we execute a different operation
            case "APIRepository":
                postAPIDataset(attachFiles, datasetDescription, repositoryId, endpoint, apiDatasetName, projectID);
                break;
            case "LocalRepository":
                postLocalDataset(attachFiles, datasetDescription, repositoryId, projectID);
                break;
            case "RelationalJDBCRepository":
                postJDBCDataset(attachTables, datasetDescription, repositoryId, projectID);
                break;
            default: // Throw an exception for unsupported file formats
                throw new FormatNotAcceptedException("Unsupported repository type: " + repository.getRepositoryType());
        }
        deleteTemporalFiles();
    }

    /**
     * Adds a new dataset to the system, coming from a local file.
     *
     * @param attachFiles        The attached files representing the datasets.
     * @param projectID          Identification of the project to which the new dataset will be added.
     * @param repositoryID       Identification of the repository to which the new dataset will belong to.
     * @param datasetDescription The description of the dataset.
     */
    public void postLocalDataset(List<MultipartFile> attachFiles, String datasetDescription, String repositoryID, String projectID) {
        if (attachFiles.isEmpty()) {
            throw new EmptyFileException("File is empty");
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
                throw new FormatNotAcceptedException("The file does not have extension and so it can not be handled");
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
            Dataset dataset = generateDataset(filePath, datasetName, datasetDescription, repositoryID, "", format);
            dataset.setUUID(UUID);
            dataset = saveDataset(dataset);

            handleDataset(dataset, repositoryID, projectID);
        }
    }

    /**
     * Adds a new dataset to the system, coming from an API.
     *
     * @param attachFiles        The attached files representing the datasets.
     * @param projectID          Identification of the project to which the new dataset will be added.
     * @param repositoryID       Identification of the repository to which the new dataset will belong to.
     * @param datasetDescription The description of the dataset.
     * @param endpoint           Endpoint of the URL to get the data from
     * @param apiDatasetName     Name given to the dataset by the user in the frontend
     */
    public void postAPIDataset(List<MultipartFile> attachFiles, String datasetDescription, String repositoryID, String endpoint, String apiDatasetName, String projectID) {
        if (attachFiles.isEmpty()) {
            throw new EmptyFileException("File is empty");
        }
        for (MultipartFile attachFile : attachFiles) {
            String UUID = generateUUID(); // Unique universal identifier (UUID) of the dataset
            String newFileName = UUID + ".api"; // New file name using the UUID

            // Reconstruct file from the Multipart file (i.e. store the file in the temporal zone to be accessed later)
            String filePath = storeTemporalFile(attachFile, newFileName);

            // Generate dataset, set UUID parameter and save it (to assign an id)
            Dataset dataset = generateDataset(filePath, apiDatasetName, datasetDescription, repositoryID, endpoint, "api");
            dataset.setUUID(UUID);
            dataset = saveDataset(dataset);

            handleDataset(dataset, repositoryID, projectID);
        }
    }

    /**
     * Adds a new dataset to the system, coming from an API.
     *
     * @param attachTables       The attached tables representing the datasets.
     * @param projectID          Identification of the project to which the new dataset will be added.
     * @param repositoryID       Identification of the repository to which the new dataset will belong to.
     * @param datasetDescription The description of the dataset.
     */
    public void postJDBCDataset(List<String> attachTables, String datasetDescription, String repositoryID, String projectID) {
        for (String tableName : attachTables) {
            // Extract data from datasource file, set UUID parameter and save it (to assign an id)
            String UUID = generateUUID(); // Unique universal identifier (UUID) of the dataset
            Dataset dataset = generateDataset(null, tableName, datasetDescription, repositoryID, null, "sql");
            dataset.setUUID(UUID);
            dataset = saveDataset(dataset);

            handleDataset(dataset, repositoryID, projectID);
        }
    }

    /**
     * Stores a multipart file in the specified disk path and returns the absolute path of the file.
     *
     * @param multipartFile     The multipart file to store.
     * @param newFileDirectory  Directory of the new file
     * @return The absolute path of the stored file.
     */
    public String storeTemporalFile(MultipartFile multipartFile, String newFileDirectory) {
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        return dataLayerInterFace.storeTemporalFile(multipartFile, newFileDirectory);
    }

    /**
     * Creates a dataset object corresponding to the original data/file/format.
     *
     * @param filePath           The path of the file to extract data from.
     * @param datasetName        The name of the dataset.
     * @param datasetDescription The description of the dataset.
     * @param repositoryID       Identifier of the repository
     * @param endpoint           The endpoint of the URL used to get the data (when coming from an API).
     * @param format             Format used by the data, which will define the dataset that we create.
     * @return A Dataset object with the extracted data.
     */
    public Dataset generateDataset(String filePath, String datasetName, String datasetDescription, String repositoryID, String endpoint, String format) {
        Dataset dataset;
        DataRepository repository = repositoryService.getRepositoryById(repositoryID);

        switch (format) { // Create a new dataset object with the extracted data based on the format
            case "csv":
                dataset = new CSVDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "json":
                dataset = new JSONDataset(null, datasetName, datasetDescription, filePath);
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
                dataset = new XMLDataset(null, datasetName, datasetDescription, filePath);
                break;
            case "parquet":
                dataset = new ParquetDataset(null, datasetName, datasetDescription, filePath);
                break;
            default: // Throw an exception for unsupported file formats
                throw new FormatNotAcceptedException("Unsupported file format: " + format);
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

    /**
     * Manages the data from the dataset, executing the required preprocesses needed before the integration tasks. These
     * include bootstrapping to get the wrapper and the graph, generating the visual representation and storing the data.
     *
     * @param dataset            Newly created dataset that represents the data.
     * @param repositoryID       Identifier of the repository
     * @param projectID          Identification of the project to which the new dataset will be added.
     */
    private void handleDataset(Dataset dataset, String repositoryID, String projectID) {
        try {
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
            saveGraphToDatabase(graph);

            // If dataset is materialized, store it permanently in the data layer (unless there is an error)
            DataRepository repository = repositoryService.getRepositoryById(repositoryID);
            if (!repository.getVirtual()) {
                uploadToDataLayer(datasetWithGraph);
            }
            saveDataset(datasetWithGraph);

        } catch (Exception e) {
            deleteDatasetFromProject(projectID, dataset.getId());
            throw new InternalServerErrorException("Error when uploading the data to the data layer", e.getMessage());
        }
    }

    /**
     * Extracts the attributes of the dataset from the wrapper. This needs to be done, as the wrapper dictates which
     * are the attributes of the dataset.
     *
     * @param wrapper wrapper obtained from the bootstrapping process of the dataset
     * @return A Dataset object with the extracted data.
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

    /**
     * Generates an Attribute class from a name.
     *
     * @param attributeName name of the new attribute.
     * @return An Attribute object with the indicated name.
     */
    private Attribute generateAttribute(String attributeName) {
        return new Attribute(attributeName, "string");
    }


    /**
     * Transforms a Dataset object into a Graph object representing the data in RDF format.
     *
     * @param dataset The Dataset object to transform.
     * @return A GraphModelPair object containing the transformed Graph and the corresponding Model.
     */
    public BootstrapResult bootstrapDataset(Dataset dataset) {
        bsModuleInterface bsInterface = new bsModuleImpl();
        return bsInterface.bootstrapDataset(dataset);
    }

    /**
     * Generates a visual representation of a Graph using the NextiaGraphy library.
     *
     * @param graph The GraphModelPair object containing the Graph.
     * @return A String which codifies the visual schema of the Graph (the frontend will interpret the String).
     */
    public String generateVisualSchema(Graph graph) {
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        return visualLibInterface.generateVisualGraph(graph);
    }

    /**
     * Saves a Graph object to the database using a GraphStoreInterface.
     *
     * @param graph The Graph object to save.
     */
    public void saveGraphToDatabase(Graph graph) {
        GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);
        graphStore.saveGraph(graph); // Save the Graph object to the database
    }

    /**
     * Deletes a dataset from the specified project.
     *
     * @param projectId The ID of the project to delete the dataset from.
     * @param datasetId The ID of the dataset to delete.
     */
    public void deleteDatasetFromProject(String projectId, String datasetId) {
        projectService.deleteDatasetFromProject(projectId, datasetId);
    }

    /**
     * Deletes all the temporal files used to create a dataset (i.e. it removes the folders)
     */
    public void deleteTemporalFiles() {
        DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
        dlInterface.deleteTemporalFiles();
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
     * Edits a dataset's attributes in the database
     *
     * @param datasetID          Identification of the dataset to be edited
     * @param datasetName        New name to be given to the dataset.
     * @param datasetDescription New description to be given to the dataset.
     */
    public void editDataset(String datasetID, String datasetName, String datasetDescription) {
        Dataset originalDataset = getDatasetById(datasetID);

        originalDataset.setDatasetName(datasetName);
        originalDataset.setDatasetDescription(datasetDescription);

        saveDataset(originalDataset);
    }

    /**
     * Retrieves a dataset by its unique identifier and returns it with its associated graph.
     *
     * @param datasetID The unique identifier of the dataset to retrieve.
     * @return The dataset object with its associated graph.
     */
    public Dataset getDatasetById(String datasetID) {
        // Retrieve the dataset by its unique identifier
        Dataset dataset = ormDataResource.findById(Dataset.class, datasetID);
        if (dataset == null) {
            throw new ElementNotFoundException("Dataset not found with ID: " + datasetID);
        }

        // Retrieve the content of the graph associated with the dataset
        GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);

        Graph datasetGraph = null;
        if (dataset.getLocalGraph() != null && dataset.getLocalGraph().getGraphName() != null)
            datasetGraph = graphStore.getGraph(dataset.getLocalGraph().getGraphName());

        // Set the local graph of the dataset to the retrieved graph
        dataset.setLocalGraph((LocalGraphJenaImpl) datasetGraph);

        return dataset;
    }

    /**
     * Uploads a dataset in the data layer, persisting the data of the original files
     *
     * @param dataset The dataset whose data will be stored.
     */
    public void uploadToDataLayer(Dataset dataset) {
        DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
        dlInterface.uploadToDataLayer(dataset);
    }

    /**
     * Downloads the schema of a dataset, in .ttl format.
     *
     * @param datasetID Identification of the dataset whose schema will be downloaded
     * @return A ResponseEntity with the headers and the schema
     */
    public ResponseEntity<InputStreamResource> downloadDatasetSchema(String datasetID) {
        Dataset dataset = getDatasetById(datasetID);

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

    /**
     * Assigns the schema of a dataset to the integrated graph of a project.
     *
     * @param projectID The ID of the project to which the schema is assigned.
     * @param datasetID The ID of the dataset whose schema is assigned to the project.
     */
    public void setDatasetSchemaAsProjectSchema(String projectID, String datasetID) {
        Project project = projectService.getProject(projectID);
        Dataset dataset = getDatasetById(datasetID);

        // Assign the schema of the dataset to the project's INTEGRATED GRAPH
        Graph integratedGraph = CoreGraphFactory.createIntegratedGraph();
        GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);

        Graph datasetGraph = graphStore.getGraph(dataset.getLocalGraph().getGraphName());

        integratedGraph.setGraphName(null);
        integratedGraph.setGraph(datasetGraph.getGraph());
        integratedGraph.setGraphicalSchema(datasetGraph.getGraphicalSchema());

        project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedGraph);
        projectService.saveProject(project);

        project.setIntegratedDatasets(new ArrayList<>()); // remove the previous integrated datasets
        addIntegratedDataset(project, datasetID); // add the new dataset to the list of integrated datasets
        projectService.saveProject(project);
    }

    /**
     * Adds a dataset to the list of integrated datasets of a project
     *
     * @param project The ID of the project to which the new (integrated) dataset will be added.
     * @param datasetID The ID of the dataset that will be integrated into the project graph.
     */
    public Project addIntegratedDataset(Project project, String datasetID) {
        List<Dataset> integratedDatasets = project.getIntegratedDatasets();
        Dataset dataset = getDatasetById(datasetID);
        integratedDatasets.add(dataset); // Add the new dataset to the list of integrated datasets
        project.setIntegratedDatasets(integratedDatasets); // Update the list of integrated datasets in the project
        return project;
    }

    /**
     * Adds a dataset to the list of (temporal) integrated datasets of a project
     *
     * @param project The ID of the project to which the new (integrated) dataset will be added (temporally).
     * @param datasetID The ID of the dataset that will be (temporally) integrated into the project graph.
     */
    public Project addTemporalIntegratedDataset(Project project, String datasetID) {
        List<Dataset> temporalIntegratedDatasets = project.getTemporalIntegratedDatasets();
        Dataset dataset = getDatasetById(datasetID);
        temporalIntegratedDatasets.add(dataset); // Add the new dataset to the list of integrated datasets
        project.setTemporalIntegratedDatasets(temporalIntegratedDatasets); // Update the list of integrated datasets in the project
        return project;
    }

    /**
     * Downloads a file, obtained from the data of a given URL.
     *
     * @param url URL where the desired data is found.
     * @return A ResponseEntity with the headers and the content downloaded from the URL.
     */
    public ResponseEntity<ByteArrayResource> downloadFileFromURL(String url) {
        // Parse URL to obtain file name
        URL fileUrl;
        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new ElementNotFoundException("The URL was malformed");
        }
        String fileName = Paths.get(fileUrl.getPath()).getFileName().toString();

        // HTTP request to obtain the data
        byte[] fileContent = restTemplate.getForObject(url, byte[].class);

        if (fileContent != null && fileContent.length > 0) {
            // Header configuration
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            ByteArrayResource resource = new ByteArrayResource(fileContent); // ByteArrayResource from the data
            // Return the response with the Multipart file
            return ResponseEntity.ok().headers(headers).body(resource);
        } else {
            throw new ElementNotFoundException("The URL was not found");
        }
    }

    /**
     * Downloads data obtained from an API (in JSON format)
     *
     * @param url URL where the desired data is found.
     * @return A ResponseEntity with the headers and the desired content of the API.
     */
    public ResponseEntity<byte[]> makeRequestFromURL(String url) {
        // Execute HTTP request and get the data in a byte array (byte[])
        byte[] responseBytes = restTemplate.getForObject(url, byte[].class);

        if (responseBytes != null && responseBytes.length > 0) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.ok().headers(headers).body(responseBytes); // Return JSON file
        } else {
            throw new ElementNotFoundException("The URL content could not be found");
        }
    }
}


