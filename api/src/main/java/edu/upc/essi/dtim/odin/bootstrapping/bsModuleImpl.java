package edu.upc.essi.dtim.odin.bootstrapping;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
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

        if (dataset.getClass().equals(CsvDataset.class)) {
            bootstrapInterface = new CSVBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset.getClass().equals(JsonDataset.class)) {
            bootstrapInterface = new JSONBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset.getClass().equals(SQLDataset.class)) {
            bootstrapInterface = new SQLBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset.getClass().equals(XmlDataset.class)) {
            bootstrapInterface = new XMLBootstrap_with_DataFrame_MM_without_Jena();
        } else if (dataset.getClass().equals(ParquetDataset.class)) {
            bootstrapInterface = new ParquetBootstrap_with_DataFrame_MM_without_Jena();
        }

        return bootstrapInterface.bootstrap(dataset);
    }
}
