package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationData;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationTemporalResponse;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import edu.upc.essi.dtim.odin.project.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for handling integration operations.
 */
@RestController
public class IntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);

    private final IntegrationService integrationService;

    /**
     * Constructs a new instance of IntegrationController.
     *
     * @param integrationService the IntegrationService dependency for performing integration operations
     */
    IntegrationController(@Autowired IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    /**
     * Handles the integration of datasets for a project.
     *
     * @param projectId The ID of the project.
     * @param iData     The IntegrationData containing datasets and alignments.
     * @return A ResponseEntity containing the IntegrationTemporalResponse or an error status.
     */
    @PostMapping(value = "/project/{id}/integration")
    public ResponseEntity<IntegrationTemporalResponse> integrate(@PathVariable("id") String projectId,
                                                                 @RequestBody IntegrationData iData) {
        logger.info("INTEGRATING temporal with project: "+projectId);

        Project project = integrationService.getProject(projectId);

        int totalDatasets = 0;

        // Count the total number of datasets within all repositories of the project
        for (DataRepository repository : project.getRepositories()) {
            totalDatasets += repository.getDatasets().size();
        }

        // Check if there are enough datasets to integrate in the project
        if (totalDatasets > 1) {
            // Integrate the new data source onto the existing integrated graph and overwrite it
            Graph integratedGraph = integrationService.integrateData(project.getIntegratedGraph(), iData.getDsB(), iData.getAlignments());

            String path = "..\\api\\dbFiles\\ttl\\";

            Project projectToSave = integrationService.updateIntegratedGraphProject(project, integratedGraph);

            Graph globalGraph = integrationService.generateGlobalGraph(project.getIntegratedGraph(), iData.getDsB(), iData.getAlignments());
            projectToSave=integrationService.updateGlobalGraphProject(projectToSave, globalGraph);

            Project project1 = integrationService.saveProject(projectToSave);
            logger.info("PROJECT SAVED WITH THE NEW INTEGRATED GRAPH");
            Project project2 = integrationService.getProject(project1.getProjectId());

            List<JoinAlignment> joinProperties =  integrationService.generateJoinAlignments(project.getIntegratedGraph(), (Graph) iData.getDsB().getLocalGraph(), iData);
            System.out.println(joinProperties);
            for(int i = 0; i < joinProperties.size(); ++i){
                System.out.println(joinProperties.get(i));
            }

            return new ResponseEntity<>(new IntegrationTemporalResponse(project2, joinProperties), HttpStatus.OK);
        }
        else{
            // If there are not enough datasets to integrate, return a bad request status
            return new ResponseEntity<>(new IntegrationTemporalResponse(null,null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles the integration of join alignments into the project's integrated graph.
     *
     * @param id     The ID of the project.
     * @param joinA  The list of JoinAlignment objects representing the join alignments to integrate.
     * @return A ResponseEntity containing the updated Project with integrated joins or an error status.
     */
    @PostMapping(value = "/project/{id}/integration/join")
    public ResponseEntity<Project> integrateJoins(@PathVariable("id") String id, @RequestBody List<JoinAlignment> joinA){

        logger.info("INTEGRATING joins...");

        Project project = integrationService.getProject(id);
        System.out.println(project.getIntegratedGraph().getGlobalGraph().getGraphicalSchema()+"DDDDDDDDDDDDDDDDDDDDDDDDDDD");

        // Integrate the join alignments into the integrated graph
        Graph integratedSchema = integrationService.joinIntegration(project.getIntegratedGraph(), joinA);

        // Update the project's integrated graph with the integrated schema
        //project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedSchema);
        project = integrationService.updateIntegratedGraphProject(project, integratedSchema);

        // Integrate the join alignments into the global schema
        Graph globalSchema = integrationService.joinIntegration(project.getIntegratedGraph(), joinA);

        // Set the global graph of the project's integrated graph
        //project.getIntegratedGraph().setGlobalGraph((GlobalGraphJenaImpl) globalSchema);
        project = integrationService.updateGlobalGraphProject(project, globalSchema);

        // Save and return the updated project
        Project savedProject = integrationService.saveProject(project);

        savedProject = integrationService.getProject(savedProject.getProjectId());
        System.out.println(savedProject.getIntegratedGraph().getGlobalGraph().getGraphicalSchema());


        return new ResponseEntity(savedProject, HttpStatus.OK);
    }


    /**
     * Accepts and persists the integration results for a specific project.
     *
     * @param id The ID of the project for which integration results are accepted and persisted.
     * @return A ResponseEntity containing the updated Project with integrated data or an error status.
     */
    @PostMapping(value = "/project/{id}/integration/persist")
    public ResponseEntity<Project> acceptIntegration(@PathVariable("id") String id) {
        // TODO: Delete this call
        // Currently, this method returns the Project by calling integrationService.getProject(id)
        return new ResponseEntity(integrationService.getProject(id), HttpStatus.OK);
    }

    /**
     * Persists a dataset as a data source for a specific project.
     *
     * @param id         The ID of the project where the dataset will be persisted as a data source.
     * @param dataSource The dataset to be persisted as a data source.
     * @return A ResponseEntity containing the persisted Dataset or an error status.
     */
    @PostMapping(value = "/project/{id}/datasources/persist")
    public ResponseEntity<Dataset> persistDataSource(@PathVariable("id") String id, @RequestBody Dataset dataSource) {
        // TODO: Delete this call
        // Currently, this method returns the input Dataset without performing any actual persistence logic.
        return new ResponseEntity<>(dataSource, HttpStatus.CREATED);
    }

}