package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.NextiaGraphy;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

/**
 * The controller class for managing datasources in a project.
 */
@RestController
public class SourceController {
    private static final Logger logger = LoggerFactory.getLogger(SourceController.class);
    private final SourceService sourceService;
    private final AppConfig appConfig;

    /**
     * Constructs a new instance of SourceController.
     *
     * @param sourceService the SourceService dependency for performing datasource operations
     */
    SourceController(@Autowired SourceService sourceService, @Autowired AppConfig appConfig) {
        this.sourceService = sourceService;
        this.appConfig = appConfig;
    }

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFileFromURL(@RequestParam String url) {
        try {
            // Parse URL to obtain file name
            URL fileUrl = new URL(url);
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
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/makeRequest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> makeRequestFromURL(@RequestParam String url) {
        logger.info("Make request to URL received: " + url);
        try {
            // Execute HTTP request and get the data in a byte array (byte[])
            byte[] responseBytes = restTemplate.getForObject(url, byte[].class);

            if (responseBytes != null && responseBytes.length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Return JSON file
                return ResponseEntity.ok().headers(headers).body(responseBytes);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("URL content could not be found".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error in request: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Performs a bootstrap operation by creating a datasource, transforming it into a graph, and saving it to the database.
     *
     * @param datasetDescription The description of the dataset.
     * @param attachFiles        The attached files representing the datasources.
     * @return A ResponseEntity object containing the saved dataset or an error message.
     */
    @PostMapping(value = "/project/{id}")
    public ResponseEntity<Object> bootstrap(@PathVariable("id") String projectId,
                                            @RequestParam String repositoryId,
                                            @RequestParam(required = false) String apiDatasetName,
                                            @RequestParam(required = false) String endpoint,
                                            @RequestParam(required = false) String datasetDescription,
                                            @RequestPart(required = false) List<MultipartFile> attachFiles,
                                            @RequestParam(required = false) List<String> attachTables) {
        try {
            System.out.println("API DATASET NAME: " + apiDatasetName);
            logger.info("Datasource received for bootstrap: " + repositoryId);

            // Get the repository object associated to the new dataset
            DataRepository repository = sourceService.findRepositoryById(repositoryId);

            // If attachTables is empty it means that we either have a local file or a file coming from an API (which is
            // stored as a json file). Otherwise, we have data coming from a sql database.
            if (!attachTables.isEmpty()) {
                handleAttachTables(attachTables, datasetDescription, repository, projectId);
            } else {
                handleAttachFiles(attachFiles, datasetDescription, repository, endpoint, apiDatasetName, projectId);
            }

            // Return success message
            return new ResponseEntity<>(null, HttpStatus.OK);

        } catch (UnsupportedOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data source not created successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the data source");
        }
    }

    private void handleAttachFiles(List<MultipartFile> attachFiles, String datasetDescription, DataRepository repository, String endpoint, String apiDatasetName, String projectId) {
        if (attachFiles.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        // Iterate through the list of MultipartFiles to handle each file (the user might have uploaded several files at once)
        for (MultipartFile attachFile : attachFiles) {
            String UUID = sourceService.generateUUID(); // Unique universal identifier (UUID) of the dataset
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
            String filePath = sourceService.storeTemporalFile(attachFile, newFileName);

            // Specific handling of API calls. If the file comes from an API, there is no name, and we only receive ".json".
            // The file that has been created in the temporal zone is a json, which is needed to obtain/execute the wrapper.
            // However, we now change the format to create the correct dataset
            if (fullFileName.equals(".json")) {
                format = "api";
                datasetName = apiDatasetName;
            }

            // Extract data from datasource file, set UUID parameter and save it
            Dataset dataset = sourceService.generateDataset(filePath, datasetName, datasetDescription, repository, endpoint, format);
            dataset.setUUID(UUID);
            sourceService.saveDataset(dataset);

            handleDataset(dataset, repository.getVirtual(), repository.getId(), projectId);
        }
    }

    private void handleAttachTables(List<String> attachTables, String datasetDescription, DataRepository repository, String projectId) {
        for (String tableName : attachTables) {
            // Extract data from datasource file, set UUID parameter and save it
            Dataset dataset = sourceService.generateDataset(null, tableName, datasetDescription, repository, null, "sql");
            String UUID = sourceService.generateUUID(); // Unique universal identifier (UUID) of the dataset
            dataset.setUUID(UUID);
            sourceService.saveDataset(dataset);

            handleDataset(dataset, repository.getVirtual(), repository.getId(), projectId);
        }
    }

    private void handleDataset(Dataset dataset, Boolean isVirtual, String repositoryId, String projectId) {
        // Add the dataset to the repository and delete the reference from others if exists
        dataset = sourceService.addRepositoryToDataset(dataset.getId(), repositoryId);
        sourceService.addDatasetToRepository(dataset.getId(), repositoryId);

        // Transform datasource into graph and generate the wrapper
        BootstrapResult bsResult = sourceService.bootstrapDataset(dataset);
        Graph graph = bsResult.getGraph();

        // Generating visual schema for frontend
        String visualSchema = sourceService.generateVisualSchema(graph);
        graph.setGraphicalSchema(visualSchema);

        // Set wrapper to the dataset and add the attributes based on the wrapper
        String wrapper = bsResult.getWrapper();
        dataset.setWrapper(wrapper);
        dataset.setAttributes(sourceService.getAttributesFromWrapper(wrapper));

        // Create the relation with dataset adding the graph generated to generate an id
        Dataset datasetWithGraph = sourceService.setLocalGraphToDataset(dataset, graph);
        graph.setGraphName(datasetWithGraph.getLocalGraph().getGraphName());

        // Save graph into the database
        sourceService.saveGraphToDatabase(graph);

        // If dataset is materialized, store it permanently in the data layer (unless there is an error)
        if (!isVirtual) {
            boolean error = sourceService.uploadToDataLayer(datasetWithGraph);
            if (error) {
                sourceService.deleteDatasetFromProject(projectId, dataset.getId());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The was an error reading the data. Dataset not created");
            }
        }
    }

    /**
     * Deletes a datasource from a specific project.
     *
     * @param projectId The ID of the project from which to delete the datasource.
     * @param id        The ID of the datasource to delete.
     * @return A ResponseEntity object containing a boolean indicating if the deletion was successful or not.
     */
    @DeleteMapping("/project/{projectId}/datasource/{id}")
    public ResponseEntity<Boolean> deleteDataset(@PathVariable("projectId") String projectId,
                                                 @PathVariable("id") String id) {
        logger.info("Delete dataset " + id + " from project: ", projectId);
        boolean deleted = false;

        //Check if the dataset is part of that project
        if (sourceService.projectContains(projectId, id)) {
            // Delete dataset from the dataLayer (parquet files and tables) and from the project (ODIN database)
            sourceService.deleteDatasetFromProject(projectId, id);
            deleted = true;
        }

        if (deleted) {
            return ResponseEntity.ok(true); // HTTP status 200 (OK) and the boolean value true
        } else {
            return ResponseEntity.notFound().build(); // HTTP status 404 (Not Found)
        }
    }

    /**
     * Retrieves all datasources from a specific project.
     *
     * @param id The ID of the project to retrieve datasources from.
     * @return A ResponseEntity object containing the list of datasets or an error message.
     */
    @GetMapping("/project/{id}/datasources")
    public ResponseEntity<Object> getDatasourcesFromProject(@PathVariable String id) {
        try {
            logger.info("GET ALL DATASOURCE FROM PROJECT {}", id);
            List<Dataset> datasets = sourceService.getDatasetsOfProject(id);

            if (datasets.isEmpty()) {
                return new ResponseEntity<>("There are no datasets yet", HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(datasets, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all repositories from a specific project.
     *
     * @param id The ID of the project to retrieve repositories from.
     * @return A ResponseEntity object containing the list of repositories or an error message.
     */
    @GetMapping("/project/{id}/repositories")
    public ResponseEntity<Object> getRepositoriesFromProject(@PathVariable String id) {
        try {
            logger.info("GET ALL repositories FROM PROJECT {}", id);

            // Retrieve a list of repositories associated with the project
            List<DataRepository> repositories = sourceService.getRepositoriesOfProject(id);

            if (repositories.isEmpty()) {
                // If there are no repositories, return a response with "No content" status
                return new ResponseEntity<>("There are no repositories yet", HttpStatus.NO_CONTENT);
            }

            // Return the list of repositories with "OK" status
            return new ResponseEntity<>(repositories, HttpStatus.OK);
        } catch (Exception e) {
            // Handle exceptions and return an error response if an error occurs
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves all datasources.
     *
     * @return A ResponseEntity object containing the list of datasets or an error message.
     */
    @GetMapping("/datasources")
    public ResponseEntity<Object> getAllDatasource() {
        try {
            logger.info("GET ALL DATASOURCE RECEIVED");

            // Retrieve a list of all datasets
            List<Dataset> datasets = sourceService.getDatasets();

            if (datasets.isEmpty()) {
                // If there are no datasets, return a response with "Not Found" status
                return new ResponseEntity<>("No datasets found", HttpStatus.NOT_FOUND);
            }

            // Return the list of datasets with "OK" status
            return new ResponseEntity<>(datasets, HttpStatus.OK);
        } catch (Exception e) {
            // Handle exceptions and return an error response if an error occurs
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Edits a dataset in a specific project.
     *
     * @param projectId          The ID of the project where the dataset belongs.
     * @param datasetId          The ID of the dataset to edit.
     * @param datasetName        The new name for the dataset.
     * @param datasetDescription The new description for the dataset (optional, default is an empty string).
     * @param repositoryId       The ID of the repository where the dataset should be stored.
     * @param repositoryName     The name of the repository (used when creating a new one).
     * @return A ResponseEntity object containing a boolean indicating if the dataset was edited successfully or not.
     */
    @PostMapping("/editDataset")
    public ResponseEntity<Boolean> editDataset(@RequestParam("projectId") String projectId,
                                               @RequestParam("datasetId") String datasetId,
                                               @RequestParam("datasetName") String datasetName,
                                               @RequestParam(value = "datasetDescription", required = false, defaultValue = "") String datasetDescription,
                                               @RequestParam("repositoryId") String repositoryId,
                                               @RequestParam("repositoryName") String repositoryName) {
        // Create a new Dataset object with the provided dataset information
        Dataset dataset = new Dataset(datasetId, datasetName, datasetDescription);

        // Log the dataset ID and dataset name for debugging purposes
        logger.info("EDIT request received for editing dataset with ID: {}", dataset.getId());
        logger.info("EDIT request received for editing dataset with name: {}", dataset.getDatasetName());

        // Call the sourceService to edit the dataset and get the result
        boolean edited = sourceService.editDataset(dataset);

        // Check if the repositoryId is empty, which indicates a new repository should be created
        boolean createRepo = (repositoryId == null) || (repositoryId.equals(""));

        // Add the dataset to the repository and delete the reference from others if exists
        sourceService.addDatasetToRepository(datasetId, repositoryId);

        // Check if the dataset was edited successfully
        if (edited) {
            // Return a ResponseEntity with HTTP status 200 (OK) and the boolean value true
            return ResponseEntity.ok(true);
        } else {
            // Return a ResponseEntity with HTTP status 404 (Not Found)
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Downloads the schema of a specific dataset as a Turtle (.ttl) file.
     *
     * @param projectID The ID of the project that contains the dataset.
     * @param datasetId The ID of the dataset to download the schema for.
     * @return A ResponseEntity object containing the Turtle schema file or a "Not Found" response if the dataset doesn't exist.
     */
    @GetMapping("/project/{id}/datasources/download/datasetschema")
    public ResponseEntity<InputStreamResource> downloadDatasetSchema(
            @PathVariable("id") String projectID,
            @RequestParam("dsID") String datasetId
    ) {
        // Get the dataset by its ID
        Dataset dataset = sourceService.getDatasetById(datasetId);

        if (dataset == null) {
            // If the dataset doesn't exist, return a "Not Found" response
            return ResponseEntity.notFound().build();
        }

        // Get the RDF model (graph) from the dataset
        Model model = dataset.getLocalGraph().getGraph();
        StringWriter writer = new StringWriter();

        // Write the model (graph) to a StringWriter in Turtle format
        model.write(writer, "TTL");

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
     * Sets the dataset schema as the project schema.
     *
     * @param projectID The ID of the project.
     * @param datasetID The ID of the dataset whose schema should be set as the project schema.
     * @return ResponseEntity containing the API response.
     */
    @PostMapping("/project/{projectID}/dataset/{datasetID}/setProjectSchema")
    public ResponseEntity<?> setDatasetSchemaAsProjectSchema(
            @PathVariable("projectID") String projectID,
            @PathVariable("datasetID") String datasetID
    ) {
        logger.info("SET PROJECT {projectID} SCHEMA request received", projectID);
        sourceService.setProjectSchemasBase(projectID, datasetID);
        sourceService.deleteIntegratedDatasets(projectID);
        sourceService.addIntegratedDataset(projectID, datasetID);

        return ResponseEntity.ok("Dataset schema set as project schema.");


        //return ResponseEntity.notFound().build();
    }

    @PostMapping("prueba")
    public ResponseEntity<String> pru(@RequestBody String path) {
        System.out.println("Generating visual graph for file: " + path);
        String visualSchemaIntegration = "";
        if (path != null) {
            Model model = RDFDataMgr.loadModel(path);
            Graph g = CoreGraphFactory.createNormalGraph();
            g.setGraph(model);
            NextiaGraphy ng = new NextiaGraphy();
//        String visualSchemaIntegration = ng.generateVisualGraph(model);
            visualSchemaIntegration = ng.generateVisualGraphNew(g);
        }

        return new ResponseEntity<>(visualSchemaIntegration, HttpStatus.OK);
    }
}

