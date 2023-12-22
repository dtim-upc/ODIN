package edu.upc.essi.dtim.odin.datasets;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.NextiaGraphy;
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
 * The controller class for managing datasets in a project.
 */
@RestController
public class DatasetController {
    private static final Logger logger = LoggerFactory.getLogger(DatasetController.class);
    private final DatasetService datasetService;

    DatasetController(@Autowired DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Adds a new dataset into the system, which requires to create the dataset object, execute a bootstrap operation,
     * transform the data into a graph and store it to the databases (ODIN and data layer).
     *
     * @param projectID          Identification of the project to which the new dataset will be added.
     * @param repositoryId       Identification of the repository to which the new dataset will belong to
     * @param attachFiles        The attached files representing the datasets (for local/API repositories).
     * @param attachTables       The SQL tables representing the datasets (for JDBC repositories).
     * @param apiDatasetName     Name given to the dataset by the user (only when the data comes from an API).
     * @param endpoint           Endpoint of the URL (only when the data comes from an API).
     * @param datasetDescription The description of the dataset.
     * @return A ResponseEntity object containing the saved dataset or an error message.
     */
    @PostMapping(value = "/project/{projectID}")
    public ResponseEntity<Object> postDataset(@PathVariable("projectID") String projectID,
                                              @RequestParam String repositoryId,
                                              @RequestPart(required = false) List<MultipartFile> attachFiles,
                                              @RequestParam(required = false) List<String> attachTables,
                                              @RequestParam(required = false) String apiDatasetName,
                                              @RequestParam(required = false) String endpoint,
                                              @RequestParam(required = false) String datasetDescription) {
        logger.info("Adding dataset to project " + projectID + " in repository " + repositoryId);
        // TODO: put all of this code in the datasetService
        try {
            DataRepository repository = datasetService.getRepositoryById(repositoryId);
            switch (repository.getRepositoryType()) { // Depending on the type of repo, we execute a different operation
                case "APIRepository":
                    datasetService.postAPIDataset(attachFiles, datasetDescription, repositoryId, endpoint, apiDatasetName, projectID);
                    break;
                case "LocalRepository":
                    datasetService.postLocalDataset(attachFiles, datasetDescription, repositoryId, projectID);
                    break;
                case "RelationalJDBCRepository":
                    datasetService.postJDBCDataset(attachTables, datasetDescription, repositoryId, projectID);
                    break;
                default: // Throw an exception for unsupported file formats
                    throw new IllegalArgumentException("Unsupported repository type: " + repository.getRepositoryType());
            }
            return new ResponseEntity<>(null, HttpStatus.OK); // Success message
        }
        catch (UnsupportedOperationException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data source not created successfully");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the data source");
        }
    }

    /**
     * Deletes a dataset from a specific project.
     *
     * @param projectID The ID of the project from which to delete the datasource.
     * @param datasetID The ID of the datasource to delete.
     * @return A ResponseEntity object containing a boolean indicating if the deletion was successful or not.
     */
    @DeleteMapping("/project/{projectID}/datasource/{datasetID}")
    public ResponseEntity<Boolean> deleteDataset(@PathVariable("projectID") String projectID,
                                                 @PathVariable("datasetID") String datasetID) {
        logger.info("Delete dataset " + datasetID + " from project: " +  projectID);
        try {
            datasetService.deleteDataset(projectID, datasetID);
            return ResponseEntity.ok(true);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build(); // HTTP status 404 (Not Found)
        }
    }

    /**
     * Edits a dataset (name of the dataset and description) in a specific project.
     *
     * @param datasetId          The ID of the dataset to edit.
     * @param datasetName        The new name for the dataset.
     * @param datasetDescription The new description for the dataset (optional, default is an empty string).
     * @return A ResponseEntity object containing a boolean indicating if the dataset was edited successfully or not.
     */
    @PostMapping("/editDataset")
    public ResponseEntity<Boolean> editDataset(@RequestParam("datasetId") String datasetId,
                                               @RequestParam("datasetName") String datasetName,
                                               @RequestParam(value = "datasetDescription", required = false, defaultValue = "") String datasetDescription) {
        logger.info("Edit request received for editing dataset with ID: " +  datasetId +  ", name: " + datasetName);

        // Call the function to edit the dataset. Returns true if it was edited, false otherwise
        boolean edited = datasetService.editDataset(datasetId, datasetName, datasetDescription);

        // Check if the dataset was edited successfully
        if (edited) {
            return ResponseEntity.ok(true); // HTTP status 200 (OK) and the boolean value true
        } else {
            return ResponseEntity.notFound().build(); // HTTP status 404 (Not Found)
        }
    }

    /**
     * Downloads the schema of a specific dataset as a Turtle (.ttl) file.
     *
     * @param datasetID The ID of the dataset to download the schema for.
     * @return A ResponseEntity object containing the Turtle schema file or a "Not Found" response if the dataset doesn't exist.
     */
    @GetMapping("/project/{id}/datasources/download/datasetschema")
    public ResponseEntity<InputStreamResource> downloadDatasetSchema(@RequestParam("dsID") String datasetID) {
        return datasetService.downloadDatasetSchema(datasetID);
    }

    /**
     * Sets the dataset schema as the project schema (it also updates the set of integrated datasets).
     *
     * @param projectID The ID of the project.
     * @param datasetID The ID of the dataset whose schema should be set as the project schema.
     * @return ResponseEntity containing the API response.
     */
    @PostMapping("/project/{projectID}/dataset/{datasetID}/setProjectSchema")
    public ResponseEntity<?> setDatasetSchemaAsProjectSchema(@PathVariable("projectID") String projectID,
                                                             @PathVariable("datasetID") String datasetID) {
        logger.info("Set project " + projectID + " schema request received for dataset" + datasetID);
        datasetService.setDatasetSchemaAsProjectSchema(projectID, datasetID);
        return ResponseEntity.ok("Dataset schema set as project schema.");
    }

    /**
     * Downloads file(s) from a given URL
     *
     * @param url URL to get the file from.
     * @return ResponseEntity containing the files(s) or an error message.
     */
    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFileFromURL(@RequestParam String url) {
        // TODO: put all of this code in the datasetService
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
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Makes a request to an online API, obtaining the data.
     *
     * @param url URL of the API to be requested.
     * @return A ResponseEntity object containing the data from the API or an error message.
     */
    @GetMapping(value = "/makeRequest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> makeRequestFromURL(@RequestParam String url) {
        // TODO: put all of this code in the datasetService
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
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error in request: " + e.getMessage()).getBytes());
        }
    }

    // TODO: remove once guaranteed that it is not useful
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

