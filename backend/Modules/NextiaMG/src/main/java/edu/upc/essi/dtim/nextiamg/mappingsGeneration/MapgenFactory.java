package edu.upc.essi.dtim.nextiamg.mappingsGeneration;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiamg.mappingsImplementation.R2RMLmapgen;


public class MapgenFactory {

    private MapgenFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    public static MapgenODIN getInstance(String mappingsType, Graph graphI) throws Exception {
        MapgenODIN instance = null;
        if (mappingsType.equals("R2RML") ) {
            instance = (MapgenODIN) new R2RMLmapgen(graphI);
        }  else {
            throw new IllegalArgumentException("Unsupported dataset type");
        }
        return instance;
    }
}
