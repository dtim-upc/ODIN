package edu.upc.essi.dtim.nextiamg.mappingsGeneration;


import edu.upc.essi.dtim.NextiaCore.graph.GlobalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.mappings.Mappings;

public interface MapgenODIN {
    MapgenResult generateMappingsResult(Mappings mappingsObj);
}
