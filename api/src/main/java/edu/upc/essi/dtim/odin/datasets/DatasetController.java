package edu.upc.essi.dtim.odin.datasets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class DatasetController {
    private static final Logger logger = LoggerFactory.getLogger(DatasetController.class);
    @Autowired
    private DatasetService datasetService;

    // ---------------- CRUD Operations
    /**
     * Adds a new dataset into the system, which requires to create the dataset object, execute a bootstrap operation,
     * transform the data into a graph and store it to the databases (ODIN and data layer).
     *
     * @param projectID          ID of the project to which the new dataset will be added.
     * @param repositoryId       ID of the repository to which the new dataset will belong to
     * @param attachFiles        The attached files representing the datasets (for local/API repositories).
     * @param attachTables       The SQL tables representing the datasets (for JDBC repositories).
     * @param apiDatasetName     Name given to the dataset by the user (only when the data comes from an API).
     * @param endpoint           Endpoint of the URL (only when the data comes from an API).
     * @param datasetDescription The description of the dataset.
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PostMapping(value = "/project/{projectID}/dataset")
    public ResponseEntity<Object> postDataset(@PathVariable("projectID") String projectID,
                                              @RequestParam String repositoryId,
                                              @RequestPart(required = false) List<MultipartFile> attachFiles,
                                              @RequestParam(required = false) List<String> attachTables,
                                              @RequestParam(required = false) String apiDatasetName,
                                              @RequestParam(required = false) String endpoint,
                                              @RequestParam(required = false) String datasetDescription) {
        logger.info("Adding dataset to project " + projectID + " in repository " + repositoryId);
        datasetService.postDataset(projectID, repositoryId, attachFiles, attachTables, apiDatasetName, endpoint, datasetDescription);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Edits a dataset (name of the dataset and description) in a specific project.
     *
     * @param datasetId          The ID of the dataset to edit.
     * @param datasetName        The new name for the dataset.
     * @param datasetDescription The new description for the dataset (optional, default is an empty string).
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PutMapping("/project/{projectID}/dataset/{datasetID}")
    public ResponseEntity<Boolean> putDataset(@PathVariable("datasetID") String datasetId,
                                              @RequestParam("datasetName") String datasetName,
                                              @RequestParam(value = "datasetDescription", required = false, defaultValue = "") String datasetDescription) {
        logger.info("Edit request received for editing dataset with ID: " +  datasetId);
        datasetService.putDataset(datasetId, datasetName, datasetDescription);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Deletes a dataset from a specific project.
     *
     * @param projectID The ID of the project from which to delete the datasource.
     * @param datasetID The ID of the datasource to delete.
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @DeleteMapping("/project/{projectID}/dataset/{datasetID}")
    public ResponseEntity<Boolean> deleteDataset(@PathVariable("projectID") String projectID,
                                                 @PathVariable("datasetID") String datasetID) {
        logger.info("Delete dataset " + datasetID + " from project: " +  projectID);
        datasetService.deleteDatasetFromProject(projectID, datasetID);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // ---------------- Schema operations
    /**
     * Downloads the schema of a specific dataset as a Turtle (.ttl) file.
     *
     * @param datasetID The ID of the dataset to download the schema for.
     * @return If the task was successful return a ResponseEntity containing the Turtle schema file.
     */
    @GetMapping("/project/{projectID}/dataset/{datasetID}/schema")
    public ResponseEntity<InputStreamResource> downloadDatasetSchema(@PathVariable("datasetID") String datasetID) {
        logger.info("Downloading schema for dataset: " +  datasetID);
        return datasetService.downloadDatasetSchema(datasetID);
    }

    /**
     * Sets the dataset schema as the project schema (it also updates the set of integrated datasets).
     *
     * @param projectID The ID of the project.
     * @param datasetID The ID of the dataset whose schema should be set as the project schema.
     * @return If the task was successful return a ResponseEntity containing the API response.
     */
    @PostMapping("/project/{projectID}/dataset/{datasetID}/set-project-schema")
    public ResponseEntity<?> setDatasetSchemaAsProjectSchema(@PathVariable("projectID") String projectID,
                                                             @PathVariable("datasetID") String datasetID) {
        logger.info("Set project " + projectID + " schema request received for dataset" + datasetID);
        datasetService.setDatasetSchemaAsProjectSchema(projectID, datasetID);
        return new ResponseEntity<>("Dataset schema set as project schema.", HttpStatus.OK);
    }

    // ---------------- Getting data from source
    /**
     * Downloads file(s) from a given URL
     *
     * @param url URL to get the file from.
     * @return If the task was successful return a ResponseEntity containing the files(s).
     */
    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFileFromURL(@RequestParam String url) {
        logger.info("Downloading file from URL: " + url);
        return datasetService.downloadFileFromURL(url);
    }

    /**
     * Makes a request to an online API, obtaining the data.
     *
     * @param url URL of the API to be requested.
     * @return If the task was successful return a ResponseEntity object containing the data from the API.
     */
    @GetMapping(value = "/make-request", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> makeRequestFromURL(@RequestParam String url) {
        logger.info("Make request to URL received: " + url);
        return datasetService.makeRequestFromURL(url);
    }
}

