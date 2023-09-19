package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.DataResource;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.CsvDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.JsonDataset;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.upc.essi.dtim.nextiabs.CSVBootstrap_with_DataFrame_MM_without_Jena;
import edu.upc.essi.dtim.nextiabs.JSONBootstrap_with_DataFrame_MM_without_Jena;
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
    @Override
    public Graph convertDatasetToGraph(DataResource dataset) {
        int bsVersion = 1;

        Graph bootstrapG = CoreGraphFactory.createGraphInstance("normal");

        if(bsVersion == 0) {
            // Bloque de código deprecado
            Model bootstrapM = convertDatasetToModel(dataset);
            bootstrapG.setGraph(bootstrapM);
        } else if (bsVersion == 1) {
            /* TODO: update when new BS is ready*/
            if (dataset.getClass().equals(CsvDataset.class)) {
                CSVBootstrap_with_DataFrame_MM_without_Jena bootstrap = new CSVBootstrap_with_DataFrame_MM_without_Jena(dataset.getId(), ((CsvDataset) dataset).getDatasetName(), ((CsvDataset) dataset).getPath());
                try {
                    bootstrapG = bootstrap.bootstrapSchema();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (dataset.getClass().equals(JsonDataset.class)) {
                JSONBootstrap_with_DataFrame_MM_without_Jena j = new JSONBootstrap_with_DataFrame_MM_without_Jena(dataset.getId(), ((JsonDataset) dataset).getDatasetName(), ((JsonDataset) dataset).getPath());
                try {
                    bootstrapG = j.bootstrapSchema();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else{}

        return bootstrapG;
    }

    /**
     * Convierte un conjunto de datos en un modelo RDF.
     *
     * @param dataset El conjunto de datos que se va a convertir.
     * @return Un modelo RDF que representa el conjunto de datos.
     */
    Model convertDatasetToModel(DataResource dataset) {
        Model bootstrapM = ModelFactory.createDefaultModel();
        if (dataset.getClass().equals(CsvDataset.class)) {
            CSVBootstrap bootstrap = new CSVBootstrap();
            try {
                bootstrapM = bootstrap.bootstrapSchema(((Dataset) dataset).getDatasetName(), ((Dataset) dataset).getDatasetName(), ((CsvDataset) dataset).getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (dataset.getClass().equals(JsonDataset.class)) {
            JSONBootstrapSWJ j = new JSONBootstrapSWJ();
            try {
                bootstrapM = j.bootstrapSchema(((Dataset) dataset).getDatasetName(), ((Dataset) dataset).getDatasetName(), ((JsonDataset) dataset).getPath());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return bootstrapM;
    }
}
