package edu.upc.essi.dtim.odin.NextiaStore.GraphStore;


import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.odin.OdinApplication;
import edu.upc.essi.dtim.odin.exception.CustomIOException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.RelationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleInterface;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class GraphStoreJenaImpl implements GraphStoreInterface {
    private static final Logger logger = LoggerFactory.getLogger(OdinApplication.class);
    private final String directory;

    public GraphStoreJenaImpl(@Autowired AppConfig appConfig) {
        this.directory = appConfig.getJenaPath();
        new File(directory).mkdirs(); // Create the directory to store the graphs (if it is necessary)
    }

    /**
     * Deletes the graph from the graph database (i.e. delete the corresponding .rdf file)
     *
     * @param graph graph to be removed
     */
    @Override
    public void deleteGraph(Graph graph) {
        try {
            Files.delete(Path.of(directory + graph.getGraphName() + ".rdf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves a graph into the graph database (i.e. write the graph into a .rdf file)
     *
     * @param graph graph to be saved
     */
    @Override
    public void saveGraph(Graph graph) {
        Model modelToSave = graph.getGraph();
        String modelName = graph.getGraphName();
        String filePath = directory + modelName + ".rdf";
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            modelToSave.write(fos, "RDF/XML");
            fos.close();
            logger.info("Model successfully store at: " + filePath);
        } catch (IOException e) {
            throw new CustomIOException("Error when storing the model: " + e.getMessage());
        }
    }

    /**
     * Gets a graph from the graph database.
     *
     * @param graphName name of the graph to be retrieved
     */
    @Override
    public Graph getGraph(String graphName) {
        String filePath = directory + graphName + ".rdf";
        Model model = ModelFactory.createDefaultModel();

        Graph graph;
        // Check what type of graph it is to build the interface
        ORMStoreInterface ormInterface = ORMStoreFactory.getInstance();
        if (ormInterface.findById(LocalGraphJenaImpl.class, graphName) != null) {
            graph = CoreGraphFactory.createLocalGraph();
        } else if (ormInterface.findById(IntegratedGraphJenaImpl.class, graphName) != null) {
            graph = CoreGraphFactory.createIntegratedGraph();
        } else {
            graph = CoreGraphFactory.createGraphInstance("normal");
        }

        try {
            // Get the graph
            model.read(new FileInputStream(filePath), "RDF/XML");
            graph.setGraphName(graphName);
            graph.setGraph(model);

            // Generate the visual representation
            nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
            String graphicalSchema = visualLibInterface.generateVisualGraph(graph);
            graph.setGraphicalSchema(graphicalSchema);

            // If graph is integrated, get the global graph
            if (graph.getClass().equals(IntegratedGraphJenaImpl.class)) {
                integrationModuleInterface integrationInterface = new integrationModuleImpl();
                Graph globalGraph = integrationInterface.generateGlobalGraph(graph);
                ((IntegratedGraphJenaImpl) graph).setGlobalGraph((GlobalGraphJenaImpl) globalGraph);
            }

            logger.info("Model loaded successfully from: " + filePath);
        } catch (FileNotFoundException e) {
            throw new CustomIOException("Error when loading the model: " + e.getMessage());
        }
        return graph;
    }

}

