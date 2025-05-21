package edu.upc.essi.dtim.nextiamg.mappingsImplementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.upc.essi.dtim.NextiaCore.graph.MappingsGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.mappings.MappingsConfig;
import edu.upc.essi.dtim.nextiamg.mappingsImplementation.R2RMLmapgenConfigJoin;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Test;

import java.io.*;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class R2RMLmapgenConfigJoinTest {

    @ParameterizedTest
    @CsvSource({
            "/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/integrated_graph.ttl,/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/Gold/generated_mappings.ttl,/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/config.properties",  // Test case with config file
    })
    void testGeneratedMappingsMatchReference(String inputGraphPath, String expectedGraphPath, String configurationFilePath) throws FileNotFoundException {
        // Load the integrated graph from the test data (input file)
        Graph integratedGraph = new IntegratedGraphJenaImpl();
        integratedGraph.setGraph(RDFDataMgr.loadModel(inputGraphPath));

        R2RMLmapgenConfigJoin r2RMLmapgen;

        try (InputStream configInputStream = new FileInputStream(configurationFilePath)) {
            // Create a MappingsConfig object with the configuration file
            r2RMLmapgen = new R2RMLmapgenConfigJoin(integratedGraph, configInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration file: " + configurationFilePath, e);
        }

        // Generate the mappings
        MappingsGraph generatedGraph = r2RMLmapgen.generateMappings();
        Model generatedModel = generatedGraph.getGraph();

        // Save the generated model to a file
        String outputFilePath = "/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/generated_mappings_output.ttl";  // Path where the generated model will be saved
        try (OutputStream outputStream = new FileOutputStream(outputFilePath)) {
            RDFDataMgr.write(outputStream, generatedModel, Lang.TURTLE);  // Write the RDF model as Turtle
            System.out.println("Generated mappings saved to: " + outputFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error saving generated mappings to file: " + outputFilePath, e);
        }


        // Load the reference (expected) TTL model
        Model expectedModel = RDFDataMgr.loadModel(expectedGraphPath);

        // Compare the two models
        if (!generatedModel.isIsomorphicWith(expectedModel)) {
            // Print the differences (if the test fails)
            System.out.println("Generated Model: \n" + generatedModel);
            System.out.println("Expected Model: \n" + expectedModel);
        }

        // Assertion
        assertTrue(generatedModel.isIsomorphicWith(expectedModel), "The generated RDF model must match the expected RDF model.");
    }
}
