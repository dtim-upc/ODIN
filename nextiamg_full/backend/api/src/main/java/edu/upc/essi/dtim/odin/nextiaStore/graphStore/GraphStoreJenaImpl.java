package edu.upc.essi.dtim.odin.nextiaStore.graphStore;

import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.WorkflowGraphJenaImpl;
import edu.upc.essi.dtim.odin.OdinApplication;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleInterface;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.*;

@Component
public class GraphStoreJenaImpl implements GraphStoreInterface {
    private static final Logger logger = LoggerFactory.getLogger(OdinApplication.class);
    private final Dataset dataset;

    public GraphStoreJenaImpl(@Autowired AppConfig appConfig) {
        String directory = appConfig.getJenaPath();
        new File(directory).mkdirs(); // Create the directory to store the graphs (if it is necessary)
        dataset = TDBFactory.createDataset(directory);
    }

    /**
     * Saves a graph into the graph database
     *
     * @param graph graph to be saved
     */
    @Override
    public void saveGraph(Graph graph) {
        Model modelToSave = graph.getGraph();
        dataset.begin(ReadWrite.WRITE);
        if (dataset.containsNamedModel(graph.getGraphName())) {
            dataset.removeNamedModel(graph.getGraphName());
        }
        dataset.addNamedModel(graph.getGraphName(), modelToSave);
        dataset.commit();
    }

    /**
     * Saves the given graph to the store, which is represented in a string.
     *
     * @param graph         The graph to save.
     * @param graphInString The representation of the graph in a String
     */
    @Override
    public void saveGraphFromStringRepresentation(Graph graph, String graphInString) {
        Model model = ModelFactory.createDefaultModel();
        try (StringReader stringReader = new StringReader(graphInString)) {
            // Load data from the string into the model
            RDFDataMgr.read(model, stringReader, null, Lang.TURTLE);
        }
        graph.setGraph(model);
        saveGraph(graph);
    }

    /**
     * Gets a graph from the graph database.
     *
     * @param graphName name of the graph to be retrieved
     */
    @Override
    public Graph getGraph(String graphName) {
        Graph graph;
        // Check what type of graph it is to build the interface
        ORMStoreInterface ormInterface = ORMStoreFactory.getInstance();
        if (ormInterface.findById(LocalGraphJenaImpl.class, graphName) != null) {
            graph = CoreGraphFactory.createLocalGraph();
        } else if (ormInterface.findById(IntegratedGraphJenaImpl.class, graphName) != null) {
            graph = CoreGraphFactory.createIntegratedGraph();
        } else if (ormInterface.findById(WorkflowGraphJenaImpl.class, graphName) != null) {
            graph = CoreGraphFactory.createWorkflowGraph();
        } else {
            graph = CoreGraphFactory.createGraphInstance("normal");
        }

        // Get the model from the store
        dataset.begin(ReadWrite.READ);
        Model modelOriginal = dataset.getNamedModel(graphName);
        Model model = ModelFactory.createDefaultModel();
        model.add(modelOriginal);
        dataset.end();

        // Assign the model to the graph
        graph.setGraphName(graphName);
        graph.setGraph(model);

        // Generate the visual representation if graph is other than workflowGraph
        if (!graph.getClass().equals(WorkflowGraphJenaImpl.class)) {
            nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
            String graphicalSchema = visualLibInterface.generateVisualGraph(graph);
            graph.setGraphicalSchema(graphicalSchema);
        }

        // If graph is integrated, get the global graph
        if (graph.getClass().equals(IntegratedGraphJenaImpl.class)) {
            integrationModuleInterface integrationInterface = new integrationModuleImpl();
            Graph globalGraph = integrationInterface.generateGlobalGraph(graph);
            ((IntegratedGraphJenaImpl) graph).setGlobalGraph((GlobalGraphJenaImpl) globalGraph);
        }

        logger.info("Model loaded successfully");
        return graph;
    }


    /**
     * Deletes the graph from the graph database
     *
     * @param graph graph to be removed
     */
    @Override
    public void deleteGraph(Graph graph) {
        dataset.begin(ReadWrite.WRITE);
        if (graph != null && graph.getGraphName() != null) {
            dataset.removeNamedModel(graph.getGraphName());
        }
        dataset.commit();
    }

    @Override
    public Graph changeGraphName(String graphName, String oldString, String newString) {
//        Model model = dataset.getNamedModel(graphName);
//        dataset.begin(ReadWrite.WRITE);
//
//        String namedGraphURI = "http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/" + graphName;
//
//        DatasetGraph dsg = dataset.asDatasetGraph();
//
//        // SPARQL Update query
//        String sparqlUpdate = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//                                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//                                "DELETE {?s ?p ?o} " +
//                                "INSERT {?s ?p ?oNew} " +
//                                "WHERE {?s ?p ?o . BIND(REPLACE(str(?o), '" + oldString + "', '" + newString + "', 'i') AS ?oNew)}";
//
//        // Execute the update inside a transaction
//        try {
//            dataset.begin(ReadWrite.WRITE);
//            UpdateProcessor processor = UpdateExecutionFactory.create(sparqlUpdate, dataset);
//            processor.execute();
//            dataset.commit();
//        } finally {
//            dataset.end();
//        }




//        try {
//            // Define the update query
//            String updateQuery =
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//                            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//                            "PREFIX yourGraphURI: <Your_Graph_URI> " +
//                            "DELETE {?s ?p ?o} " +
//                            "INSERT {?s ?p ?oNew} " +
//                            "WHERE {?s ?p ?o . BIND(REPLACE(str(?o), '" + oldString + "', '" + newString + "', 'i') AS ?oNew)}";
//
//            // Execute the update query
//            UpdateFactory.create(updateQuery).execute(dataset);
//
//            // Commit the changes
//            dataset.commit();
//        } finally {
//            dataset.end();
//        }

        // Close the dataset when done
//        dataset.commit();
//        dataset.end();

        return null;
    }

}

