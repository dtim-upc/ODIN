package edu.upc.essi.dtim.odin.dataProducts;

import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.queries.DataProduct;
import edu.upc.essi.dtim.NextiaCore.queries.Intent;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class DataProductService {
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ProjectService projectService;

    // ---------------- CRUD/ORM operations

    /**
     * Saves a DataProduct object into the ORM store
     *
     * @param dataProduct The DataProduct object to save.
     * @return The saved DataProduct object.
     */
    public DataProduct saveDataProduct(DataProduct dataProduct) {
        return ormDataResource.save(dataProduct);
    }

    /**
     * Retrieves a data product by its unique identifier
     *
     * @param dataProductID The unique identifier of the data product to be retrieved
     * @return The data product object.
     */
    public DataProduct getDataProduct(String dataProductID) {
        DataProduct dataProduct = ormDataResource.findById(DataProduct.class, dataProductID);
        if (dataProduct == null) {
            throw new ElementNotFoundException("Data product not found with ID: " + dataProductID);
        }
        return dataProduct;
    }

    /**
     * Adds a new data product into the system.
     *
     * @param projectID                 ID of the project to which the new data product will be added.
     * @param dataProductUUID           UUID of the data product. It is computed by the system right after executing the query,
     *                                  as it allows us to directly store the data.
     * @param dataProductName           Name of the data product (given by the user)
     * @param dataProductDescription    Description of the data product (given by the user)
     * @param columns                   Set of columns of the data represented by the data product.
     *                                  We need it to generate the attributes
     */
    public void postDataProduct(String dataProductUUID, String dataProductName, String dataProductDescription, String projectID, List<String> columns) {
        DataProduct dp = new DataProduct(dataProductUUID, dataProductName, dataProductDescription);
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        dataLayerInterFace.copyToExploitationZone(dataProductUUID);

        // Create the attributes for the data product
        List<Attribute> atts = new LinkedList<>();
        for (String column: columns) {
            atts.add(new Attribute(column, "string"));
        }
        dp.setAttributes(atts);

        Project project = projectService.getProject(projectID);
        project.addDataProduct(dp);
        projectService.saveProject(project);
    }

    /**
     * Edits a data product's attributes in the database
     *
     * @param dataProductID          Identification of the data product to be edited
     * @param dataProductName        New name to be given to the data product.
     * @param dataProductDescription New description to be given to the data product.
     */
    public void putDataProduct(String dataProductID, String dataProductName, String dataProductDescription) {
        DataProduct originalDataProduct = getDataProduct(dataProductID);

        originalDataProduct.setDatasetName(dataProductName);
        originalDataProduct.setDatasetDescription(dataProductDescription);

        saveDataProduct(originalDataProduct);
    }

    /**
     * Deletes a data product from the specified project.
     *
     * @param projectID     The ID of the project to delete the data product from.
     * @param dataProductID The ID of the data product to delete.
     */
    public void deleteDataProduct(String projectID, String dataProductID) {
        Project project = projectService.getProject(projectID);
        List<DataProduct> dpOfProject = project.getDataProducts();

        List<Intent> intents = project.getIntents();
        for (Intent intent: intents) {
            if (Objects.equals(intent.getDataProduct().getId(), dataProductID)) {

            }
        }

        boolean dataProductFound = false;
        // Iterate through the data repositories
        for (DataProduct dpInProject : dpOfProject) {
            if (dpInProject.getId().equals(dataProductID)) {
                // Remove the data product from the ORM project
                dataProductFound = true;
                dpOfProject.remove(dpInProject);
                // Remove from Data layer
                DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
                dlInterface.deleteDatasetFromExploitationZone(dpInProject.getUUID());
                break;
            }
        }
        project.setDataProducts(dpOfProject); // Save and set the updated list of data repositories
        // Throw an exception if the repository was not found
        if (!dataProductFound) {
            throw new NoSuchElementException("Intent not found with id: " + dataProductID);
        }
        projectService.saveProject(project); // Save the updated project without the repository
    }

    /**
     * Retrieves the list of data products associated with a project.
     *
     * @param projectID The ID of the project.
     * @return A list of DataProduct objects belonging to the project.
     */
    public List<DataProduct> getDataProductsOfProject(String projectID) {
        Project project = projectService.getProject(projectID);
        return project.getDataProducts();
    }

    // ------------ Download/materialize operations

    /**
     * Materializes a data product into a CSV file.
     *
     * @param dataProductID The ID of the data product to be materialized
     * @return If the task was successful returns a path were the materialized file resides
     */
    public String materializeDataProduct(String dataProductID) {
        DataProduct dp = ormDataResource.findById(DataProduct.class, dataProductID);
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        return dataLayerInterFace.materialize(dp.getUUID(), "exp", "csv");
    }

    /**
     * Downloads a temporal data product. That is, just after executing a query over the data and before the data product
     * has been stored in ODIN.
     *
     * @param dataProductUUID The UUID of the data product to be downloaded
     * @return If the task was successful returns a ResponseEntity with the file to download
     */
    public ResponseEntity<FileSystemResource> downloadTemporalDataProduct(String dataProductUUID) {
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        String pathOfMaterializedDataProduct = dataLayerInterFace.materialize(dataProductUUID, "tmp_exp", "csv");
        return downloadCSVFile(pathOfMaterializedDataProduct);
    }

    /**
     * Downloads a data product.
     *
     * @param dataProductID The ID of the data product to be downloaded
     * @return If the task was successful returns a ResponseEntity with the file to download
     */
    public ResponseEntity<FileSystemResource> downloadDataProduct(String dataProductID) {
        String pathOfMaterializedDataProduct = materializeDataProduct(dataProductID);
        return downloadCSVFile(pathOfMaterializedDataProduct);
    }

    /**
     * Downloads a CSV file generated from a data product
     *
     * @param pathOfMaterializedDataProduct Path of the data product (in CSV format) to download
     * @return If the task was successful returns a ResponseEntity with the file to download
     */
    public ResponseEntity<FileSystemResource> downloadCSVFile(String pathOfMaterializedDataProduct) {
        // Create a FileSystemResource to represent the CSV file
        FileSystemResource file = new FileSystemResource(new File(pathOfMaterializedDataProduct));

        // Set headers to trigger file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=file.csv");

        // Set the content type
        headers.setContentType(MediaType.parseMediaType("text/csv"));

        // Return ResponseEntity with the file content and headers
        return ResponseEntity.ok().headers(headers).body(file);
    }
}
