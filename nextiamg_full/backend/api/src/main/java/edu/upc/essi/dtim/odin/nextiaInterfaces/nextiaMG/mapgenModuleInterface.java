package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaMG;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.mappings.Mappings;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenResult;

public interface mapgenModuleInterface {
    /**
     *
     */
    MapgenResult generateMappings(Mappings mappingsObj);
}
