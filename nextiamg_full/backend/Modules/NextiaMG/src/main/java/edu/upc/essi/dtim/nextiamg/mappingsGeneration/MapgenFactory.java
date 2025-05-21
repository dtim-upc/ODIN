package edu.upc.essi.dtim.nextiamg.mappingsGeneration;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.mappings.Mappings;
import edu.upc.essi.dtim.NextiaCore.mappings.MappingsConfig;
import edu.upc.essi.dtim.nextiamg.mappingsImplementation.R2RMLmapgen;
import edu.upc.essi.dtim.nextiamg.mappingsImplementation.R2RMLmapgenConfigJoin;


public class MapgenFactory {

    private static MapgenODIN instance = null;

    private MapgenFactory() {
        // Private constructor prevents instantiation from outside the class
    }

    // configpath should eventually evolve to a config object
    public static MapgenODIN getInstance(Mappings mappingsObj) throws Exception {
        String mappingsType = mappingsObj.getMappingType();
        if (mappingsType.equals("R2RML") ) {
            instance = new R2RMLmapgen(mappingsObj.getGraphI());
        } else if (mappingsType.equals("R2RML-CONFIG") ) {
            instance = new R2RMLmapgenConfigJoin(mappingsObj.getGraphI(), ((MappingsConfig) mappingsObj).getConfigFile());
        }  else {
            throw new IllegalArgumentException("Unsupported dataset type");
        }
        return instance;
    }
}
