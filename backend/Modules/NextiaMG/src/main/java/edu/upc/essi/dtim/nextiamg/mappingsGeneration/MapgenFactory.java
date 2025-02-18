package edu.upc.essi.dtim.nextiamg.mappingsGeneration;

import edu.upc.essi.dtim.NextiaCore.datasets.*;
import edu.upc.essi.dtim.NextiaCore.graph.GlobalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.repositories.RelationalJDBCRepository;
import edu.upc.essi.dtim.nextiamg.mappingsImplementation.R2RMLmapgen;
import edu.upc.essi.dtim.nextiamg.utils.R2RML;


public class MapgenFactory {
    private static MapgenODIN instance = null;

    private MapgenFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    public static MapgenODIN getInstance(String mappingsType, Graph graphI) throws Exception {
        if (mappingsType.equals("R2RML") ) {
            instance = (MapgenODIN) new R2RMLmapgen(graphI);
        }  else {
            throw new IllegalArgumentException("Unsupported dataset type");
        }
        return instance;
    }
}
