package edu.upc.essi.dtim.odin.NextiaStore.GraphStore;


import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.odin.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.integration.integrationModuleImpl;
import edu.upc.essi.dtim.odin.integration.integrationModuleInterface;
import org.apache.hadoop.shaded.javax.xml.bind.SchemaOutputResolver;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@Component
public class GraphStoreJenaImpl implements GraphStoreInterface {
    private Dataset dataset;
    private final String directory;

    public GraphStoreJenaImpl(@Autowired AppConfig appConfig) {
        this.directory = appConfig.getJenaPath();

        //Open TDB Dataset
        dataset = TDBFactory.createDataset(directory);
    }

    /**
     * Deletes the graph with the given name.
     *
     * @param name the URI of the graph to delete
     */
    @Override
    public void deleteGraph(URI name) {
        dataset.begin(ReadWrite.WRITE);
        try {
            String modelName = name.getURI();
            if (dataset.containsNamedModel(modelName)) {
                dataset.removeNamedModel(modelName);
            } else {
                throw getIllegalArgumentException(name);
            }
            dataset.commit();
        } catch (final Exception ex) {
            dataset.abort();
            throw ex;
        }
    }

    private static IllegalArgumentException getIllegalArgumentException(URI name) {
        return new IllegalArgumentException("Graph " + name.getURI() + " not found");
    }


    @Override
    public void saveGraph(Graph graph) {
        Model modelToSave = graph.getGraph();
        String modelName = graph.getGraphName();
        String filePath = directory + modelName + ".rdf";
        try {
            modelToSave.write(new FileOutputStream(filePath), "RDF/XML");
            System.out.println("Modelo guardado exitosamente en: " + filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Error al guardar el modelo: " + e.getMessage());
        }
    }

    @Override
    public Graph getGraph(String modelName) {
        String filePath = directory + modelName + ".rdf";
        Model model = ModelFactory.createDefaultModel();
        Graph graph;

        //miramos qué tipo de grafo es para constuir la interficie
        ORMStoreInterface ormInterface = ORMStoreFactory.getInstance();
        if(ormInterface.findById(LocalGraphJenaImpl.class, modelName) != null){
            graph = CoreGraphFactory.createLocalGraph();
        } else if (ormInterface.findById(IntegratedGraphJenaImpl.class, modelName) != null) {
            graph = CoreGraphFactory.createIntegratedGraph();
        } else {
            graph = CoreGraphFactory.createGraphInstance("normal");
        }

        try {
            model.read(new FileInputStream(filePath), "RDF/XML");
            graph.setGraphName(modelName);
            graph.setGraph(model);

            nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
            String graphicalSchema = visualLibInterface.generateVisualGraph(graph);
            graph.setGraphicalSchema(graphicalSchema);

            if (graph.getClass().equals(IntegratedGraphJenaImpl.class)){
                integrationModuleInterface integrationInterface = new integrationModuleImpl();
                Graph globalGraph = integrationInterface.generateGlobalGraph(graph);
                ((IntegratedGraphJenaImpl) graph).setGlobalGraph((GlobalGraphJenaImpl) globalGraph);
            }

            System.out.println("Modelo cargado exitosamente desde: " + filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Error al cargar el modelo: " + e.getMessage());
        }
        return graph;
    }



    /**
     * Saves the given graph.
     *
     * @param graph the graph to save
     *//*
    @Override
    public void saveGraph(Graph graph) {
        Model modelToSave = graph.getGraph();
        dataset.begin(ReadWrite.WRITE);
        try {
            String modelName = "http://example.com/"+graph.getGraphName();
            dataset.addNamedModel(modelName, modelToSave);
            dataset.commit();
            dataset.end();
            System.out.println("+++++++++++++++++++GRAFO SALVADOOOOO "+graph.getGraphName());
        } catch (final Exception ex) {
            dataset.abort();
            throw ex;
        }
    }
    */



    /**
     * Retrieves the graph with the given name.
     *
     * @param name the URI of the graph to retrieve
     * @return the retrieved graph
     */
    /*
    @Override
    public Graph getGraph(String name) {
        dataset.begin(ReadWrite.READ);
        try {
            //Retrieve Named Graph from Dataset, or use Default Graph.
            String modelName = "http://example.com/"+name;
            if (dataset.containsNamedModel(modelName)) {
                Model model = dataset.getNamedModel(modelName);
                Graph graph = CoreGraphFactory.createGraphInstance("normal");
                graph.setGraphName(name);
                graph.setGraph(model);

                nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
                String graphicalSchema = visualLibInterface.generateVisualGraph(graph);
                graph.setGraphicalSchema(graphicalSchema);

                return graph;
            } else {
                throw getIllegalArgumentException(new URI(name));
            }
        } catch (final Exception ex) {
            dataset.abort();
            throw ex;
        } finally {
            dataset.end();
        }
    }

     */

}

