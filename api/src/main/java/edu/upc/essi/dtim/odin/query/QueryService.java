package edu.upc.essi.dtim.odin.query;

import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleInterface;
import edu.upc.essi.dtim.odin.project.Project;
import edu.upc.essi.dtim.odin.project.ProjectService;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.RDFSResult;
import edu.upc.essi.dtim.odin.repositories.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    ProjectService projectService;

    public QueryService(@Autowired ProjectService projectService) {
        this.projectService = projectService;
    }

    public RDFSResult getQueryResult(QueryDataSelection body, String projectId) {

        Project project = projectService.getProjectById(projectId);

        qrModuleInterface qrInterface = new qrModuleImpl();
        return qrInterface.makeQuery(project.getIntegratedGraph(), project.getIntegratedDatasets(), body);
    }
}
