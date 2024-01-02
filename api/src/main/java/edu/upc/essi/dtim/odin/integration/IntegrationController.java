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

import java.util.List;

@RestController
public class IntegrationController {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);
    @Autowired
    private  IntegrationService integrationService;

    /**
     * Handles the integration of datasets for a project. That is, for two datasets and a set of alignments, it
     * generates the integrated graph of the graphs of the datasets and a set of JoinAlignments.
     *
     * @param projectID The ID of the project.
     * @param iData     The IntegrationData containing datasets and alignments.
     * @return A ResponseEntity containing the IntegrationTemporalResponse (i.e. the project and a set of joins).
     */
    @PostMapping(value = "/project/{projectID}/integration") // After alignments are computed
    public ResponseEntity<IntegrationTemporalResponse> integrate(@PathVariable("projectID") String projectID,
                                                                 @RequestBody IntegrationData iData) {
        logger.info("Integrating temporal of project: " + projectID);
        IntegrationTemporalResponse result = integrationService.integrate(projectID, iData);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Handles the integration of join alignments into the project's integrated graph.
     *
     * @param projectID      The ID of the project.
     * @param joinAlignments The list of JoinAlignment objects representing the join alignments to integrate.
     * @return A ResponseEntity containing the updated Project with integrated joins or an error status.
     */
    @PostMapping(value = "/project/{projectID}/integration/join") // If some joins need reviewing
    public ResponseEntity<Project> integrateJoins(@PathVariable("projectID") String projectID,
                                                  @RequestBody List<JoinAlignment> joinAlignments) {
        logger.info("Reviewing joins in project " + projectID);
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

    /**
     * Sends a request to compute the automatic alignments between two datasets
     *
     * @param projectID The ID of the project. The integrated graph of the project will be one of the datasets used
     *                  to compute the alignments.
     * @param datasetID The ID of the second dataset used to compute the alignments.
     * @return A ResponseEntity containing the updated Project with integrated data or an error status.
     */
    @PostMapping(value = "/project/{projectID}/integration/survey")
    public ResponseEntity<List<Alignment>> getAutomaticAlignments(@PathVariable("projectID") String projectID,
                                                                  @RequestBody String datasetID) {
        logger.info("Generating automatic alignments in project " + projectID);
        List<Alignment> alignments = integrationService.getAlignments(projectID, datasetID);
        return new ResponseEntity<>(alignments, HttpStatus.OK);
    }

}