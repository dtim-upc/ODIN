package edu.upc.essi.dtim.odin.dataProducts;

import edu.upc.essi.dtim.odin.intents.IntentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DataProductController {
    private static final Logger logger = LoggerFactory.getLogger(IntentController.class);
    @Autowired
    private DataProductService dataProductService;

    // TODO: Description
    @PostMapping("/project/{projectID}/data-product")
    public ResponseEntity<Boolean> postDataProduct(@PathVariable("projectID") String projectID,
                                                     @RequestParam("dataProductUUID") String dataProductUUID,
                                                     @RequestParam("dataProductName") String dataProductName,
                                                     @RequestParam("columns") List<String> columns) {
        logger.info("Storing data product");
        dataProductService.postDataProduct(dataProductUUID, dataProductName, projectID, columns);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // TODO: Description
    @PostMapping("/project/{projectID}/data-products/{dataProductID}/materialize")
    public ResponseEntity<String> materializeDataProduct(@PathVariable("projectID") String projectID,
                                                         @PathVariable("dataProductID") String dataProductID) {
        logger.info("Materializing data product");
        String pathOfMaterializedDataProduct = dataProductService.materializeDataProduct(dataProductID);
        return new ResponseEntity<>(pathOfMaterializedDataProduct, HttpStatus.OK);
    }
}
