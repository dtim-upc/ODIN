package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationData;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationTemporalResponse;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import edu.upc.essi.dtim.odin.projects.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller class for handling integration operations.
 */
@RestController
public class IntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);
    @Autowired
    private IntegrationService integrationService;

    /**
     * Handles the integration of datasets for a project.
     *
     * @param projectID The ID of the project.
     * @param iData     The IntegrationData containing datasets and alignments.
     * @return A ResponseEntity containing the IntegrationTemporalResponse or an error status.
     */
    @PostMapping(value = "/project/{projectID}/integration") // after computing the alignments
    public ResponseEntity<IntegrationTemporalResponse> integrate(@PathVariable("projectID") String projectID,
                                                                @RequestBody IntegrationData iData) {
        logger.info("Integrating temporal with project: " + projectID);
        IntegrationTemporalResponse response =  integrationService.integrate(projectID, iData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Handles the integration of join alignments into the project's integrated graph.
     *
     * @param id    The ID of the project.
     * @param joinA The list of JoinAlignment objects representing the join alignments to integrate.
     * @return A ResponseEntity containing the updated Project with integrated joins or an error status.
     */
    @PostMapping(value = "/project/{id}/integration/join") // if there are some alignments to review
    public ResponseEntity<Project> integrateJoins(@PathVariable("id") String id, @RequestBody List<JoinAlignment> joinA) {

        logger.info("Integrating joins...");

        Project project = integrationService.getProject(id);

        // Integrate the join alignments into the integrated graph
        Graph integratedSchema = integrationService.joinIntegration(project.getTemporalIntegratedGraph(), joinA);

        // Update the project's integrated graph with the integrated schema
        project = integrationService.updateTemporalIntegratedGraphProject(project, integratedSchema);

        // Integrate the join alignments into the global schema
        Graph globalSchema = integrationService.joinIntegration(project.getTemporalIntegratedGraph(), joinA);

        // Set the global graph of the project's integrated graph
        project = integrationService.updateTemporalGlobalGraphProject(project, globalSchema);

        //project = integrationService.addIntegratedDataset(datasetId);

        // Save and return the updated project
        Project savedProject = integrationService.saveProject(project);

        savedProject = integrationService.getProject(savedProject.getProjectId());


        return new ResponseEntity<>(savedProject, HttpStatus.OK);
    }


    /**
     * Accepts and persists the integration results for a specific project.
     *
     * @param id The ID of the project for which integration results are accepted and persisted.
     * @return A ResponseEntity containing the updated Project with integrated data or an error status.
     */
    @PostMapping(value = "/project/{id}/integration/persist") // finish integration process
    public ResponseEntity<Project> acceptIntegration(@PathVariable("id") String id) {
        Project temporalProject = integrationService.getProject(id);
        Project projectToSave = integrationService.updateIntegratedGraphProject(temporalProject, temporalProject.getTemporalIntegratedGraph());

        projectToSave = integrationService.updateGlobalGraphProject(projectToSave, temporalProject.getTemporalIntegratedGraph().getGlobalGraph());

        Project project1 = integrationService.saveProject(projectToSave);

        List<Dataset> temporalIntegratedDatasets = project1.getTemporalIntegratedDatasets();
        String lastDatasetIdAdded = temporalIntegratedDatasets.get(temporalIntegratedDatasets.size()-1).getId();

        project1 = integrationService.addIntegratedDataset(project1.getProjectId(), lastDatasetIdAdded);

        //todo delete temporalDatasetsList

        logger.info("Project saved with the new integrated graph");
        return new ResponseEntity(integrationService.getProject(id), HttpStatus.OK);
    }

    @PostMapping(value = "/project/{id}/integration/survey")
    public ResponseEntity<List<Alignment>> getAutomaticAlignments(@PathVariable("id") String projectId, @RequestBody String datasetId) throws SQLException, IOException, ClassNotFoundException {
        logger.info("Automatic alignments petition received");
        List<Alignment> alignments = integrationService.getAlignments(projectId, datasetId);
        if (alignments.isEmpty()) return new ResponseEntity<>(alignments, HttpStatus.NO_CONTENT);
        else return new ResponseEntity<>(alignments, HttpStatus.OK);
    }

}