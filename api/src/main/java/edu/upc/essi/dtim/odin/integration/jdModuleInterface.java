package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.nextiadi.models.Alignment;

import java.util.List;

public interface jdModuleInterface {

    List<Alignment> getAlignments(Dataset dataset, Dataset dsB);
}
