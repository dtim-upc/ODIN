package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;

import java.util.List;

public interface jdModuleInterface {
    /**
     * Gets the set of alignments between two datasets, that is, the degree of similarity between every column of both
     * sets of data.
     *
     * @param dataset The first dataset
     * @param dataset2 The second dataset
     * @return A List of the Alignments, each Alignment indicating the names of the columns (one of each dataset) and
     * the degree of similarity.
     */
    List<Alignment> getAlignments(Dataset dataset, Dataset dataset2);
}
