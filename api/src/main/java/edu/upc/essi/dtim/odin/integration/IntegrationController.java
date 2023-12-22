package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
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

@RestController
public class IntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);

    @Autowired
    private  IntegrationService integrationService;

    /**
     * Handles the integration of datasets for a project.
     *
     * @param projectId The ID of the project.
     * @param iData     The IntegrationData containing datasets and alignments.
     * @return A ResponseEntity containing the IntegrationTemporalResponse or an error status.
     */
    @PostMapping(value = "/project/{id}/integration") // After alignments are computed
    public ResponseEntity<IntegrationTemporalResponse> integrate(@PathVariable("id") String projectId,
                                                                 @RequestBody IntegrationData iData) {
        logger.info("Integrating temporal of project: " + projectId);
        IntegrationTemporalResponse result = integrationService.integrate(projectId, iData);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Handles the integration of join alignments into the project's integrated graph.
     *
     * @param projectID    The ID of the project.
     * @param joinAlignments The list of JoinAlignment objects representing the join alignments to integrate.
     * @return A ResponseEntity containing the updated Project with integrated joins or an error status.
     */
    @PostMapping(value = "/project/{projectID}/integration/join") // If some joins need reviewing
    public ResponseEntity<Project> integrateJoins(@PathVariable("projectID") String projectID, @RequestBody List<JoinAlignment> joinAlignments) {
        logger.info("Reviewing joins...");
        Project savedProject = integrationService.reviewJoins(projectID, joinAlignments);
        return new ResponseEntity<>(savedProject, HttpStatus.OK);
    }

    /**
     * Accepts and persists the integration results for a specific project.
     *
     * @param projectID The ID of the project for which integration results are accepted and persisted.
     * @return A ResponseEntity containing the updated Project with integrated data or an error status.
     */
    @PostMapping(value = "/project/{projectID}/integration/persist") // To finish the process
    public ResponseEntity<Project> acceptIntegration(@PathVariable("projectID") String projectID) {
        logger.info("Persisting project integration");
        Project savedProject = integrationService.acceptIntegration(projectID);
        return new ResponseEntity<>(savedProject, HttpStatus.OK);
    }

    @PostMapping(value = "/project/{id}/integration/survey")
    public ResponseEntity<List<Alignment>> getAutomaticAlignments(@PathVariable("id") String projectId, @RequestBody String datasetId) throws SQLException, IOException, ClassNotFoundException {
        logger.info("Generating automatic alignments");
        List<Alignment> alignments = integrationService.getAlignments(projectId, datasetId);
        return new ResponseEntity<>(alignments, HttpStatus.OK);
    }

}