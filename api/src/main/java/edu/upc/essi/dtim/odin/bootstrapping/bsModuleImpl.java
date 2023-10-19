package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.*;


/**
 * Implementación de la interfaz para realizar la conversión de un conjunto de datos a un grafo.
 */
public class bsModuleImpl implements bsModuleInterface{

    /**
     * Convierte un conjunto de datos en un grafo.
     *
     * @param dataset El conjunto de datos que se va a convertir.
     * @return Un grafo que representa el conjunto de datos.
     */
    public Graph convertDatasetToGraph(Dataset dataset) {
        NextiaBootstrapInterface bootstrapInterface = null;

        try {
            bootstrapInterface = BootstrapFactory.getInstance(dataset);
            return bootstrapInterface.bootstrap(dataset);
        } catch (Exception e) {
            throw new RuntimeException("Error converting dataset to graph: " + e.getMessage(), e);
        }
    }

}
