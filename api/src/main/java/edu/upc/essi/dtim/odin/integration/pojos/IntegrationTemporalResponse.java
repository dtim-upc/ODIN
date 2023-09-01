package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.odin.project.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * A POJO (Plain Old Java Object) class representing a response for integration with temporal information.
 */
@Data
@AllArgsConstructor
public class IntegrationTemporalResponse {

    /**
     * The project associated with the integration response.
     */
    private Project project;

    /**
     * A list of join alignments related to the integration.
     */
    private List<JoinAlignment> joins;
}
