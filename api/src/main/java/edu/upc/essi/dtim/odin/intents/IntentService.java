package edu.upc.essi.dtim.odin.intents;

import edu.upc.essi.dtim.NextiaCore.queries.DataProduct;
import edu.upc.essi.dtim.NextiaCore.queries.Intent;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.dataProducts.DataProductService;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class IntentService {

    @Autowired
    private DataProductService dataProductService;
    @Autowired
    private ProjectService projectService;
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();

    // ---------------- CRUD/ORM operations

    /**
     * Saves an Intent object into the ORM store
     *
     * @param intent The Intent object to save.
     * @return The saved Intent object.
     */
    public Intent saveIntent(Intent intent) { return ormDataResource.save(intent); }

    /**
     * Retrieves an intent by its unique identifier
     *
     * @param intentID The unique identifier of the intent to be retrieved
     * @return The intent object.
     */
    public Intent getIntent(String intentID) {
        Intent intent = ormDataResource.findById(Intent.class, intentID);
        if (intent == null) {
            throw new ElementNotFoundException("Intent not found with ID: " + intentID);
        }

        return intent;
    }

    /**
     * Retrieves the list of intents associated with a project.
     *
     * @param projectID The ID of the project.
     * @return A list of Intent objects belonging to the project.
     */
    public List<Intent> getIntentsOfProject(String projectID) {
        Project project = projectService.getProject(projectID);
        return project.getIntents();
    }

    /**
     * Adds a new data intent into the system.
     *
     * @param projectID        ID of the project to which the new dataset will be added.
     * @param intentName       Name of the intent (given by the user)
     * @param problem          Name of the problem to be solved by the intent (given by the user)
     * @param dataProductID    ID of the product associated to the intent
     */
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

    /**
     * Edits an intent's attributes in the database
     *
     * @param intentID          Identification of the intent to be edited
     * @param intentName        New name to be given to the intent.
     */
    public void putIntent(String intentID, String intentName) {
        Intent originalIntent = getIntent(intentID);

        originalIntent.setIntentName(intentName);

        saveIntent(originalIntent);
    }

    /**
     * Deletes an intent from the specified project.
     *
     * @param projectID     The ID of the project to delete the data product from.
     * @param intentID      The ID of the intent to delete.
     */
    public void deleteIntent(String projectID, String intentID) {
        Project project = projectService.getProject(projectID);
        List<Intent> intentsOfProject = project.getIntents();
        boolean intentFound = false;
        // Iterate through the intents of the project
        for (Intent intentInProject : intentsOfProject) {
            if (intentInProject.getIntentID().equals(intentID)) {
                intentFound = true;
                intentsOfProject.remove(intentInProject);
                break;
            }
        }
        project.setIntents(intentsOfProject); // Save and set the updated list of intents
        // Throw an exception if the intent was not found
        if (!intentFound) {
            throw new NoSuchElementException("Intent not found with id: " + intentID);
        }
        projectService.saveProject(project); // Save the updated project without the repository
    }
}
