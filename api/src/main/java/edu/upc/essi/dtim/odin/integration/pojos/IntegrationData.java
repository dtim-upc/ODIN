package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A POJO (Plain Old Java Object) class representing integration data.
 */
@Getter
@Setter
public class IntegrationData {

    /**
     * The first dataset (dsA) to be integrated.
     */
    private Dataset dsA;

    /**
     * The second dataset (dsB) to be integrated.
     */
    private Dataset dsB;

    /**
     * The name of the integrated dataset.
     */
    private String integratedName;

    /**
     * A list of alignments between the datasets.
     */
    private List<Alignment> alignments;
}
