package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.odin.projects.pojo.Project;

import java.util.List;

/**
 * A POJO (Plain Old Java Object) class representing a response for integration with temporal information.
 */
public class IntegrationTemporalResponse {
    private Project project; // The project associated with the integration response.
    private List<JoinAlignment> joins; // A list of join alignments related to the integration.

    public IntegrationTemporalResponse(Project project, List<JoinAlignment> joins) {
        this.project = project;
        this.joins = joins;
    }

    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }

    public List<JoinAlignment> getJoins() {
        return joins;
    }
    public void setJoins(List<JoinAlignment> joins) {
        this.joins = joins;
    }
}
