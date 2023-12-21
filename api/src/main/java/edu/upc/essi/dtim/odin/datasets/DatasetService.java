package edu.upc.essi.dtim.odin.datasets;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
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
import edu.upc.essi.dtim.odin.projects.Project;
import edu.upc.essi.dtim.odin.projects.ProjectService;

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
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The service class for managing datasets in a project.
 */
@Service
public class DatasetService {
    private final ProjectService projectService;
    private final RepositoryService repositoryService;
    private final AppConfig appConfig;
    private final ORMStoreInterface ormDataResource;

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

    /**
     * Adds a new dataset to the system, coming from a local file.
     *
     * @param attachFiles        The attached files representing the datasets.
     * @param projectID          Identification of the project to which the new dataset will be added.
     * @param repositoryID       Identification of the repository to which the new dataset will belong to.
     * @param datasetDescription The description of the dataset.
     * @throws RuntimeException If the file is empty or an error occurs during the file storage process.
     */
    public void postLocalDataset(List<MultipartFile> attachFiles, String datasetDescription, String repositoryID, String projectID) {
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
     * @throws RuntimeException  If the file is empty or an error occurs during the file storage process.
     */
    public void postAPIDataset(List<MultipartFile> attachFiles, String datasetDescription, String repositoryID, String endpoint, String apiDatasetName, String projectID) {
        if (attachFiles.isEmpty()) {
            throw new RuntimeException("File is empty");
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
     * @throws RuntimeException  If the file is empty or an error occurs during the file storage process.
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
     * Stores a multipart file in the specified disk path with a modified filename and returns the absolute path of the file.
     *
     * @param multipartFile     The multipart file to store.
     * @param newFileDirectory  Directory of the new file
     * @return The absolute path of the stored file.
     * @throws RuntimeException If the file is empty or an error occurs during the file storage process.
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
     * @throws IllegalArgumentException if the file format is not supported.
     */
    public Dataset generateDataset(String filePath, String datasetName, String datasetDescription, String repositoryID, String endpoint, String format) {
        Dataset dataset;
        DataRepository repository = getRepositoryById(repositoryID);

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

    /**
     * Manages the data from the dataset, executing the required preprocesses needed before the integration tasks. These
     * include bootstrapping to get the wrapper and the graph, generating the visual representation and storing the data.
     *
     * @param dataset            Newly created dataset that represents the data.
     * @param repositoryID       Identifier of the repository
     * @param projectID          Identification of the project to which the new dataset will be added.
     */
    private void handleDataset(Dataset dataset, String repositoryID, String projectID) {
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
        DataRepository repository = getRepositoryById(repositoryID);
        if (!repository.getVirtual()) {
            boolean error = uploadToDataLayer(datasetWithGraph);
            if (error) {
                deleteDataset(projectID, dataset.getId());
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

    /**
     * Generates an Attribute class from a name
     *
     * @param attributeName name of the new attribute.
     * @return An Attribute object with the indicated name.
     */
    private Attribute generateAttribute(String attributeName) {
        return new Attribute(attributeName, "string");
    }

    /**
     * Generates a universal unique identifier to be assigned to a dataset
     * @return A string containing the new UUID
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
        GraphStoreInterface graphStore = GraphStoreFactory.getInstance(appConfig);
        graphStore.saveGraph(graph); // Save the Graph object to the database
    }

    /**
     * Deletes a dataset from a project using the ProjectService class.
     *
     * @param projectID The ID of the project to delete the dataset from.
     * @param datasetID The ID of the dataset to delete.
     */
    public void deleteDataset(String projectID, String datasetID) {
        if (projectContains(projectID, datasetID)) {
            deleteDatasetFromProject(projectID, datasetID);
        }
        else {
            throw new RuntimeException("Dataset " + datasetID + " does not belong to project " + projectID);
        }
    }

    /**
     * Deletes a dataset from the specified project.
     *
     * @param projectId The ID of the project to delete the dataset from.
     * @param datasetId The ID of the dataset to delete.
     * @throws IllegalArgumentException If the project with the given ID is not found.
     */
    public void deleteDatasetFromProject(String projectId, String datasetId) {
        Project project = projectService.getProject(projectId);
        List<DataRepository> repositoriesOfProject = project.getRepositories();
        boolean datasetFound = false;

        // Iterate through the data repositories
        for (DataRepository repoInProject : repositoriesOfProject) {
            // Iterate through the datasets in each data repository
            Iterator<Dataset> datasetIterator = repoInProject.getDatasets().iterator();
            while (datasetIterator.hasNext()) {
                Dataset dataset = datasetIterator.next();
                if (datasetId.equals(dataset.getId())) {
                    datasetFound = true;
                    datasetIterator.remove(); // Remove the dataset from the data repository
                    // Save the updated list of data repositories and update the project's list
                    project.setRepositories(repositoriesOfProject);
                    break;
                }
            }
        }
        // Check if the dataset was not found in any data repository
        if (!datasetFound) {
            throw new NoSuchElementException("Dataset not found with id: " + datasetId);
        }
        // Save the updated project
        projectService.saveProject(project);
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
     * @param projectID The ID of the project to check.
     * @param id        The ID of the dataset to check.
     * @return A boolean indicating whether the project contains the dataset.
     */
    public boolean projectContains(String projectID, String id) {
        return projectService.projectContains(projectID, id);
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
     * Edits a dataset's attributes in the database if any attribute has changed.
     *
     * @param datasetID          Identification of the dataset to be edited
     * @param datasetName        New name to be given to the dataset.
     * @param datasetDescription New description to be given to the dataset.
     * @return A boolean indicating whether the dataset was edited successfully.
     */
    public boolean editDataset(String datasetID, String datasetName, String datasetDescription) {
        // Get the original dataset in the database to compare the information
        Dataset originalDataset = getDatasetById(datasetID);

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
     * Retrieves a dataset by its unique identifier and returns it with its associated graph.
     *
     * @param datasetID The unique identifier of the dataset to retrieve.
     * @return The dataset object with its associated graph.
     */
    public Dataset getDatasetById(String datasetID) {
        // Retrieve the dataset by its unique identifier
        Dataset dataset = ormDataResource.findById(Dataset.class, datasetID);
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset not found with ID: " + datasetID);
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
     * @return A boolean indicating if the upload was successful.
     */
    public boolean uploadToDataLayer(Dataset dataset) {
        DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
        return dlInterface.uploadToDataLayer(dataset);
    }

    // TODO: remove the call to this function in the DatasetController and remove this function
    public DataRepository getRepositoryById(String repositoryID) {
       return repositoryService.getRepositoryById(repositoryID);
    }

    /**
     * Downloads the schema of a dataset, in .ttl format.
     *
     * @param datasetID Identification of the dataset whose schema wil be downloaded
     * @return A boolean indicating if the upload was successful.
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

        projectService.deleteIntegratedDatasets(projectID); // remove the previous integrated schema
        projectService.addIntegratedDataset(projectID, datasetID); // add the new dataset to the list of integrated datasets
    }
}


