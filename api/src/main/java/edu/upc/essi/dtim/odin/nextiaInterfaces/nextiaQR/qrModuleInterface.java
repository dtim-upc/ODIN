package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.RDFSResult;

import java.util.List;

public interface qrModuleInterface {
    RDFSResult makeQuery(IntegratedGraphJenaImpl integratedGraph, List<Dataset> integratedDatasets, QueryDataSelection body);
}
