package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.QueryResult;

import java.util.List;

public interface qrModuleInterface {
    QueryResult makeQuery(IntegratedGraphJenaImpl integratedGraph, List<Dataset> integratedDatasets, QueryDataSelection body);
}
