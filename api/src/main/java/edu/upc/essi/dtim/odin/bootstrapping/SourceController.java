package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.odin.project.Project;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * The controller class for managing datasources in a project.
 */
@RestController
public class SourceController {
    private static final Logger logger = LoggerFactory.getLogger(SourceController.class);

    private final SourceService sourceService;

    /**
     * Constructs a new instance of SourceController.
     *
     * @param sourceService the SourceService dependency for performing datasource operations
     */
    SourceController(@Autowired SourceService sourceService) {
        this.sourceService = sourceService;
    }

    /**
     * Performs a bootstrap operation by creating a datasource, transforming it into a graph, and saving it to the database.
     *
     * @param projectId          The ID of the project.
     * @param datasetName        The name of the dataset.
     * @param datasetDescription The description of the dataset.
     * @param attachFiles        The attached files representing the datasources.
     * @return A ResponseEntity object containing the saved dataset or an error message.
     */
    @PostMapping(value = "/project/{id}")
    public ResponseEntity<Object> bootstrap(@PathVariable("id") String projectId,
                                            @RequestParam String repositoryId,
                                            @RequestParam String repositoryName,
                                            @RequestParam String datasetName,
                                            @RequestParam(required = false) String datasetDescription,
                                            @RequestPart("attach_files") List<MultipartFile> attachFiles) {
        try{
            logger.info("POST DATASOURCE RECEIVED FOR BOOTSTRAP " + repositoryId);
            // Validate and authenticate access here
            //future check when adding authentification

            // Check if a new repository should be created or an existing one should be used
            boolean createRepo = (repositoryId == null) || (repositoryId.equals(""));

            // Iterate through the list of MultipartFiles to handle each file
            for (MultipartFile attachFile : attachFiles) {
                // Get the original filename of the uploaded file
                String originalFileName = attachFile.getOriginalFilename();

                // Use the original filename as the datasetName
                assert originalFileName != null;
                datasetName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));

                // Reconstruct file from Multipart file
                String filePath = sourceService.reconstructFile(attachFile);

                // Extract data from datasource file
                Dataset datasource = sourceService.extractData(filePath, datasetName, datasetDescription);

                // Saving dataset to assign an id
                Dataset savedDataset = sourceService.saveDataset(datasource);

                // Transform datasource into graph
                Graph graph = sourceService.transformToGraph(savedDataset);

                // Generating visual schema for frontend
                String visualSchema = sourceService.generateVisualSchema(graph);
                graph.setGraphicalSchema(visualSchema);

                // Create the relation with dataset adding the graph generated to generate an id
                Dataset datasetWithGraph = sourceService.setLocalGraphToDataset(savedDataset, graph);
                graph.setGraphName(datasetWithGraph.getLocalGraph().getGraphName());
                graph.write("..\\api\\dbFiles\\ttl\\"+datasetName+".ttl");

                // Save graph into the database
                sourceService.saveGraphToDatabase(graph);

                // Find/create repository
                DataRepository repository;
                if (createRepo) {
                    // Create a new repository and add it to the project
                    repository = sourceService.createRepository(repositoryName);
                    sourceService.addRepositoryToProject(projectId, repository.getId());
                    createRepo = false;
                } else {
                    // Find the existing repository using the provided repositoryId
                    repository = sourceService.findRepositoryById(repositoryId);
                }
                repositoryId = repository.getId();

                // Add the dataset to the repository and delete the reference from others if exists
                sourceService.addDatasetToRepository(datasetWithGraph.getId(), repositoryId);

                if(!sourceService.projectHasIntegratedGraph(projectId)) sourceService.setProjectSchemasBase(projectId,datasetWithGraph.getId());
            }

            // Return success message
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data source not created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the data source");
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
    public ResponseEntity<Boolean> deleteDatasource(@PathVariable("projectId") String projectId,
                                                 @PathVariable("id") String id) {
        // Print a message to indicate that the delete request was received
        logger.info("DELETE A DATASOURCE from project: {}" ,projectId);
        logger.info("DELETE A DATASOURCE RECEIVED: {}" ,id);

        boolean deleted = false;

        //Check if the dataset is part of that project
        if(sourceService.projectContains(projectId, id)){
            // Delete the relation with project
            sourceService.deleteDatasetFromProject(projectId, id);

            // Is not necessary to call the projectService to delete the project and get the result
            // since we have the cascade all call in relation one-to-many Project 1-* Dataset

            deleted = true;
        }

        // Check if the project was deleted successfully
        if (deleted) {
            // Return a ResponseEntity with HTTP status 200 (OK) and the boolean value true
            return ResponseEntity.ok(true);
        } else {
            // Return a ResponseEntity with HTTP status 404 (Not Found)
            return ResponseEntity.notFound().build();
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
            logger.info("GET ALL DATASOURCE FROM PROJECT {}" , id);
            List<Dataset> datasets = sourceService.getDatasetsOfProject(id);

            if (datasets.isEmpty()) {
                return new ResponseEntity<>("There are no datasets yet",HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(datasets, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/project/{id}/repositories")
    public ResponseEntity<Object> getRepositoriesFromProject(@PathVariable String id) {
        try {
            logger.info("GET ALL repositories FROM PROJECT {}" , id);
            List<DataRepository> repositories = sourceService.getRepositoriesOfProject(id);

            if (repositories.isEmpty()) {
                return new ResponseEntity<>("There are no datasets yet",HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(repositories, HttpStatus.OK);
        } catch (Exception e) {
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
            List<Dataset> datasets = sourceService.getDatasets();

            if (datasets.isEmpty()) {
                return new ResponseEntity<>("No datasets found", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(datasets, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/editDataset")
    public ResponseEntity<Boolean> editDataset( @RequestParam("projectId") String projectId,
                                                @RequestParam("datasetId") String datasetId,
                                                @RequestParam("datasetName") String datasetName,
                                                @RequestParam(value = "datasetDescription", required = false, defaultValue = "") String datasetDescription,
                                                @RequestParam("repositoryId") String repositoryId,
                                                @RequestParam("repositoryName") String repositoryName
    ) {
        // Create a new Dataset object with the provided dataset information
        Dataset dataset = new Dataset(datasetId, datasetName, datasetDescription);

        // Log the dataset ID and dataset name for debugging purposes
        logger.info("EDIT request received for editing dataset with ID: {}", dataset.getId());
        logger.info("EDIT request received for editing dataset with name: {}", dataset.getDatasetName());

        // Call the projectService to edit the dataset and get the result
        boolean edited = sourceService.editDataset(dataset);

        // Check if the repositoryId is empty, which indicates a new repository should be created
        boolean createRepo = (repositoryId == null) || (repositoryId.equals(""));

        // Find or create the repository
        DataRepository repository;
        if (createRepo) {
            // Create a new repository if repositoryId is empty
            repository = sourceService.createRepository(repositoryName);
            repositoryId = repository.getId();

            // Add the new repository to the project
            sourceService.addRepositoryToProject(projectId, repositoryId);
        }

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

    @GetMapping("/project/{id}/datasources/download/datasetschema")
    public ResponseEntity<InputStreamResource> downloadDatasetSchema(
            @PathVariable("id") String projectID,
            @RequestParam("dsID") String datasetId
    ) {
        Dataset dataset = sourceService.getDatasetById(datasetId);

        if (dataset == null) {
            return ResponseEntity.notFound().build();
        }

        Model model = dataset.getLocalGraph().getGraph();
        StringWriter writer = new StringWriter();
        model.write(writer, "TTL");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + dataset.getDatasetName() + ".ttl");

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(writer.toString().getBytes()));

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/turtle"))
                .body(resource);
    }
}

