package edu.upc.essi.dtim.odin.dataProducts;

import edu.upc.essi.dtim.NextiaCore.queries.DataProduct;
import edu.upc.essi.dtim.odin.intents.IntentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DataProductController {
    private static final Logger logger = LoggerFactory.getLogger(IntentController.class);
    @Autowired
    private DataProductService dataProductService;

    // ---------------- CRUD Operations

    /**
     * Retrieves all data products from a specific project.
     *
     * @param projectID The ID of the project to retrieve data products from.
     * @return A ResponseEntity object containing the list of data products or an error message.
     */
    @GetMapping("/project/{projectID}/data-products")
    public ResponseEntity<Object> getDataProductsOfProject(@PathVariable("projectID") String projectID) {
        logger.info("Getting all dataProducts from project " + projectID);
        List<DataProduct> dataProducts = dataProductService.getDataProductsOfProject(projectID);
        return new ResponseEntity<>(dataProducts, HttpStatus.OK);
    }

    /**
     * Adds a new data product into the system. That is, the result from a query executed over a graph (i.e. is
     * a subclass of a dataset, whose data comes from the merging and filtering of a bunch of datasets)
     *
     * @param projectID                 ID of the project to which the new data product will be added.
     * @param dataProductUUID           UUID of the data product. It is computed by the system right after executing the query,
     *                                  as it allows us to directly store the data.
     * @param dataProductName           Name of the data product (given by the user)
     * @param dataProductDescription    Description of the data product (given by the user)
     * @param columns                   Set of columns of the data represented by the data product.
     *                                  We need it to generate the attributes
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PostMapping("/project/{projectID}/data-product")
    public ResponseEntity<Boolean> postDataProduct(@PathVariable("projectID") String projectID,
                                                   @RequestParam("dataProductUUID") String dataProductUUID,
                                                   @RequestParam("dataProductName") String dataProductName,
                                                   @RequestParam("dataProductDescription") String dataProductDescription,
                                                   @RequestParam("columns") List<String> columns) {
        logger.info("Storing data product");
        dataProductService.postDataProduct(dataProductUUID, dataProductName, dataProductDescription, projectID, columns);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Edits a data product (name and description) in a specific project.
     *
     * @param dataProductID          The ID of the data product to edit.
     * @param dataProductName        The new name for the data product.
     * @param dataProductDescription The new description for the data product (optional, default is an empty string).
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PutMapping("/project/{projectID}/data-product/{dataProductID}")
    public ResponseEntity<Boolean> putIntent(@PathVariable("dataProductID") String dataProductID,
                                             @RequestParam("dataProductName") String dataProductName,
                                             @RequestParam("dataProductDescription") String dataProductDescription) {
        logger.info("Putting data product " + dataProductID);
        dataProductService.putDataProduct(dataProductID, dataProductName, dataProductDescription);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Deletes a data product from a specific project.
     *
     * @param projectID     The ID of the project from which to delete the data product.
     * @param dataProductID The ID of the data product to be deleted.
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @DeleteMapping("/project/{projectID}/data-product/{dataProductID}")
    public ResponseEntity<Boolean> deleteIntent(@PathVariable("projectID") String projectID,
                                                @PathVariable("dataProductID") String dataProductID) {
        logger.info("Deleting data product " + dataProductID + " from project: " +  projectID);
        dataProductService.deleteDataProduct(projectID, dataProductID);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // ---------------- Other operations
    /**
     * Materializes a data product into a CSV file, mainly to be ingested by the intent generation pipeline.
     *
     * @param dataProductID   The ID of the data product to be materialized
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PostMapping("/project/{projectID}/data-product/{dataProductID}/materialize")
    public ResponseEntity<String> materializeDataProduct(@PathVariable("dataProductID") String dataProductID) {
        logger.info("Materializing data product");
        String pathOfMaterializedDataProduct = dataProductService.materializeDataProduct(dataProductID);
        return new ResponseEntity<>(pathOfMaterializedDataProduct, HttpStatus.OK);
    }
}
