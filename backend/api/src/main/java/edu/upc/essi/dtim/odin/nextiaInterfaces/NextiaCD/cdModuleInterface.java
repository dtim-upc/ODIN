package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaCD;

import edu.upc.essi.dtim.NextiaCore.constraints.DenialConstraint;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;

import java.util.List;

public interface cdModuleInterface {
    /**
     * Gets the set of denial constraints
     *
     * @param dataset The dataset
     * @return A List of the Alignments, each Alignment indicating the names of the columns (one of each dataset) and
     * the degree of similarity.
     */
    List<DenialConstraint> getDCs(Dataset dataset);
}
