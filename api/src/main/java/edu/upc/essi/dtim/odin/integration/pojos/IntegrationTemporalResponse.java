package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.odin.project.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IntegrationTemporalResponse {

    Project project;
    List<JoinAlignment> joins;


}
