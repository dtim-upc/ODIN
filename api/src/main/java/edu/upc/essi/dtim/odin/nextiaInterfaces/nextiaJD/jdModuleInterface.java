package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;

import java.util.List;

public interface jdModuleInterface {

    List<Alignment> getAlignments(Dataset dataset, Dataset dsB);
}
