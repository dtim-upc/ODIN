package edu.upc.essi.dtim.odin.dataProducts;

import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.queries.DataProduct;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

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
        boolean intentFound = false;
        // Iterate through the data repositories
        for (DataProduct dpInProject : dpOfProject) {
            if (dpInProject.getId().equals(dataProductID)) {
                // Remove the data product from the ORM project
                intentFound = true;
                dpOfProject.remove(dpInProject);
                // Remove from Data layer
                DataLayerInterface dlInterface = new DataLayerImpl(appConfig);
                dlInterface.deleteDatasetFromExploitationZone(dpInProject.getUUID());
                break;
            }
        }
        project.setDataProducts(dpOfProject); // Save and set the updated list of data repositories
        // Throw an exception if the repository was not found
        if (!intentFound) {
            throw new NoSuchElementException("Intent not found with id: " + dataProductID);
        }
        projectService.saveProject(project); // Save the updated project without the repository
    }

    // ---------------- Other operations

    /**
     * Materializes a data product into a CSV file, mainly to be ingested by the intent generation pipeline.
     *
     * @param dataProductID   The ID of the data product to be materialized
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    public String materializeDataProduct(String dataProductID) {
        // Own function to get the data product
        DataProduct dp = ormDataResource.findById(DataProduct.class, dataProductID);
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        return dataLayerInterFace.materialize(dp, "exp", "csv");
    }
}
