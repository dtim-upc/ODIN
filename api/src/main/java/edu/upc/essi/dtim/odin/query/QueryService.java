package edu.upc.essi.dtim.odin.query;

import edu.upc.essi.dtim.NextiaCore.queries.Query;
import edu.upc.essi.dtim.NextiaCore.queries.Workflow;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleInterface;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static edu.upc.essi.dtim.odin.utils.Utils.generateUUID;

@Service
public class QueryService {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private AppConfig appConfig;
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();

    // TODO: Description
    public QueryResult getQueryResult(QueryDataSelection body, String projectId) {
        Project project = projectService.getProject(projectId);
        qrModuleInterface qrInterface = new qrModuleImpl();
        return qrInterface.makeQuery(project.getIntegratedGraph(), project.getIntegratedDatasets(), body);
    }

    // TODO: Description
    public void storeQuery(String csvPath, String queryName, String projectID, String queryLabel) {
        Query query = new Query(csvPath, queryName, generateUUID(), queryLabel);
        DataLayerInterface dataLayerInterFace = new DataLayerImpl(appConfig);
        dataLayerInterFace.storeQuery(query);

        Project project = projectService.getProject(projectID);
        project.addQuery(query);
        projectService.saveProject(project);
    }

    // TODO: Description
    public Query getQueryByID(String queryID) {
        // Retrieve the query by its unique identifier
        Query query = ormDataResource.findById(Query.class, queryID);
        if (query == null) {
            throw new ElementNotFoundException("Query not found with ID: " + queryID);
        }
        return query;
    }

    // TODO: Description
    public void saveQuery(Query query) {
        ormDataResource.save(query);
    }

    // TODO: Description
    public void storeWorkflow(String queryID, Workflow workflow) {
        Query query = getQueryByID(queryID);
        query.addWorkflow(workflow);
        saveQuery(query);
    }
}
