package edu.upc.essi.dtim.odin.query;

import edu.upc.essi.dtim.NextiaCore.queries.Query;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleInterface;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    @Autowired
    private ProjectService projectService;
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();

    // TODO: Description
    // TODO: Remake when all the pipeline is clear
    public QueryResult getQueryResult(QueryDataSelection body, String projectId) {
        Project project = projectService.getProject(projectId);
        qrModuleInterface qrInterface = new qrModuleImpl();
        return qrInterface.makeQuery(project.getIntegratedGraph(), project.getIntegratedDatasets(), body);
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


}
