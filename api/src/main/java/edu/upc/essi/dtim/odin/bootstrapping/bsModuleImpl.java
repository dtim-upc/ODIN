package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.CsvDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.JsonDataset;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.nextiabs.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.upc.essi.dtim.nextiadi.bootstraping.CSVBootstrap;
import edu.upc.essi.dtim.nextiadi.bootstraping.JSONBootstrapSWJ;

import java.io.FileNotFoundException;
import java.io.IOException;

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
        NextiaBootstrapInterface bootstrapInterface = new NextiaBootstrapImpl();
        return bootstrapInterface.bootstrap(dataset);
    }
}
