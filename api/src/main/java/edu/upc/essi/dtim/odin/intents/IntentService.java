package edu.upc.essi.dtim.odin.intents;

import edu.upc.essi.dtim.NextiaCore.queries.DataProduct;
import edu.upc.essi.dtim.NextiaCore.queries.Intent;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.dataProducts.DataProductService;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntentService {

    @Autowired
    private DataProductService dataProductService;
    @Autowired
    private ProjectService projectService;
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();

    // TODO: description
    public Intent saveIntent(Intent intent) { return ormDataResource.save(intent); }

    // TODO: description
    public Intent getIntent(String intentID) {
        Intent intent = ormDataResource.findById(Intent.class, intentID);
        if (intent == null) {
            throw new ElementNotFoundException("Intent not found with ID: " + intentID);
        }

        return intent;
    }

    // TODO: description
    public String postIntent(String intentName, String problem, String projectID, String dataProductID) {
        Intent newIntent = new Intent(intentName, problem);
        DataProduct dp = dataProductService.getDataProduct(dataProductID);
        newIntent.setDataProduct(dp);
        newIntent = saveIntent(newIntent);

        Project project = projectService.getProject(projectID);
        project.addIntent(newIntent);
        projectService.saveProject(project);

        return newIntent.getIntentID();
    }
}
