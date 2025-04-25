package edu.upc.essi.dtim.nextiamg.mappingsGeneration;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiamg.mappingsImplementation.R2RMLmapgen;
import edu.upc.essi.dtim.nextiamg.mappingsImplementation.R2RMLmapgenConfigJoin;


public class MapgenFactory {

    private MapgenFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    // configpath should eventually evolve to a config object
    public static MapgenODIN getInstance(String mappingsType, Graph graphI, String configPath) throws Exception {
        MapgenODIN instance = null;
        if (mappingsType.equals("R2RML") ) {
            instance = (MapgenODIN) new R2RMLmapgen(graphI);
        } else if (mappingsType.equals("R2RML-CONFIG") ) {
            instance = (MapgenODIN) new R2RMLmapgenConfigJoin(graphI, configPath);
        }  else {
            throw new IllegalArgumentException("Unsupported dataset type");
        }
        return instance;
    }
}
