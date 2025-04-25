package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaMG;

import edu.upc.essi.dtim.NextiaCore.graph.GlobalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;

import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenFactory;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenODIN;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenResult;

public class mapgenModuleImpl implements mapgenModuleInterface {

    public MapgenResult generateMappings(String mappingType, Graph graphI, String configPath) {
        MapgenODIN mapgenODINInterface;
        try {
            // configpath should eventually evolve to a config object
            mapgenODINInterface = MapgenFactory.getInstance(mappingType, graphI, configPath);
            return mapgenODINInterface.generateMappingsResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Error converting dataset to graph", e.getMessage());
        }
    }

}
