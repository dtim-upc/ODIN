package edu.upc.essi.dtim.odin.integration.pojos;

//import edu.upc.essi.dtim.odin.bootstrapping.DataSource;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class IntegrationData {
    private Dataset dsA;
    private Dataset dsB;
    private String integratedName;
    private List<Alignment> alignments;
}
