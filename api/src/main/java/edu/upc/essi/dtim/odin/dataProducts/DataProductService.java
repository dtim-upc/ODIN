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

@Service
public class DataProductService {
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ProjectService projectService;

    // TODO: Description
    public DataProduct getDataProduct(String dataProductID) {
        DataProduct dataProduct = ormDataResource.findById(DataProduct.class, dataProductID);
        if (dataProduct == null) {
            throw new ElementNotFoundException("Data product not found with ID: " + dataProductID);
        }

        return dataProduct;
    }

    // TODO: Description
    public void postDataProduct(String dataProductUUID, String dataProductName, String projectID, List<String> columns) {
        DataProduct dp = new DataProduct(dataProductUUID, dataProductName);
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        dataLayerInterFace.copyToExploitationZone(dataProductUUID);

        // Assign attributes
        List<Attribute> atts = new LinkedList<>();
        for (String column: columns) {
            atts.add(new Attribute(column, "string"));
        }
        dp.setAttributes(atts);

        Project project = projectService.getProject(projectID);
        project.addDataProduct(dp);
        projectService.saveProject(project);
    }

    // TODO: Description
    public String materializeDataProduct(String dataProductID) {
        // Own function to get the data product
        DataProduct dp = ormDataResource.findById(DataProduct.class, dataProductID);
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        return dataLayerInterFace.materialize(dp, "exp", "csv");
    }
}
