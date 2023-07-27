package edu.upc.essi.dtim.odin.NextiaStore.GraphStore;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.Triple;
import edu.upc.essi.dtim.NextiaCore.graph.URI;
import edu.upc.essi.dtim.odin.config.AppConfig;
import org.junit.jupiter.api.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class GraphStoreJenaImplTest {

    private GraphStoreJenaImpl graphStore;
    private Dataset dataset;
    private URI graphName;

    @BeforeEach
    void setUp() {
        // Mock AppConfig
        AppConfig appConfig = Mockito.mock(AppConfig.class);
        when(appConfig.getJenaPath()).thenReturn("test-directory");

        // Create the GraphStoreJenaImpl instance
        graphStore = new GraphStoreJenaImpl(appConfig);

        // Create a mock Dataset
        dataset = Mockito.mock(Dataset.class);

        // Create a mock GraphModelPair and URI
        graphModelPair = new GraphModelPair(new LocalGraph(), getHardcodedModel());
        graphName = new URI("http://test.com/test");
        graphModelPair.getGraph().setName(graphName);

        // Reset the dataset before each test
        Mockito.doAnswer(invocation -> {
            dataset.abort();
            return null;
        }).when(dataset).begin(ReadWrite.WRITE);


    }

    @Test
    void testSaveGraph_ThrowsException() {
        // Mock the behavior to throw an exception when adding the named model
        graphModelPair.getGraph().setName(new URI());
        // Assert that an exception is thrown
        Assertions.assertThrows(RuntimeException.class, () -> graphStore.saveGraph(graphModelPair));
    }

    @Test
    void testGetGraph() {
        graphStore.saveGraph(graphModelPair);
        // Call the getGraph method
        Graph g = graphStore.getGraph(graphName);
        // Verify the retrieved graph is not null
        Assertions.assertNotNull(g);
    }

    @Test
    void testGetGraph_exception_notFound() {
        URI notFoundName = new URI("random");

        // Call the getGraph method and handle the exception
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> graphStore.getGraph(notFoundName));

        // Verify the exception message
        Assertions.assertEquals("Graph " + notFoundName.getURI() + " not found", exception.getMessage());
    }

    @Test
    void testDeleteGraph() {
        graphStore.saveGraph(graphModelPair);
        // Call the deleteGraph method
        graphStore.deleteGraph(graphName);

        // Call the getGraph method and handle the exception
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> graphStore.getGraph(graphName));

        // Verify graph is deleted
        Assertions.assertEquals("Graph " + graphName.getURI() + " not found", exception.getMessage());
    }

    @Test
    void testDeleteGraph_exception() {
        URI notFoundName = new URI("random");
        // Call the deleteGraph method and handle the exception
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> graphStore.deleteGraph(notFoundName));

        // Verify the exception message
        Assertions.assertEquals("Graph " + notFoundName.getURI() + " not found", exception.getMessage());
    }

    @Test
    void testAdaptModel() {
        // Create a mock Model
        Model model = ModelFactory.createDefaultModel();

        // Add some statements to the model
        Resource subject = model.createResource("http://example.com/subject");
        Property predicate = model.createProperty("http://example.com/predicate");
        Resource object = model.createResource("http://example.com/object");
        model.add(subject, predicate, object);

        // Call the adapt method
        URI graphName = new URI("http://example.com/graph");
        Graph adaptedGraph = graphStore.adapt(model, graphName);

        // Verify the adapted graph
        Assertions.assertEquals(graphName, adaptedGraph.getName());
        Assertions.assertEquals(1, adaptedGraph.getTriples().size());
        Triple triple = adaptedGraph.getTriples().iterator().next();
        Assertions.assertEquals(subject.getURI(), triple.getSubject().getURI());
        Assertions.assertEquals(predicate.getURI(), triple.getPredicate().getURI());
        Assertions.assertEquals(object.getURI(), triple.getObject().getURI());
    }

    @Test
    void testAdaptGraph() {
        // Create a mock Graph
        Set<Triple> triples = new HashSet<>();
        URI subjectURI = new URI("http://example.com/subject");
        URI predicateURI = new URI("http://example.com/predicate");
        URI objectURI = new URI("http://example.com/object");
        triples.add(new Triple(subjectURI, predicateURI, objectURI));
        Graph graph = new LocalGraph(null, new URI("http://example.com/graph"), triples);

        // Call the adapt method
        Model adaptedModel = graphStore.adapt(graph);

        // Verify the adapted model
        Assertions.assertEquals(1, adaptedModel.size());
        StmtIterator iter = adaptedModel.listStatements();
        Statement statement = iter.next();
        Assertions.assertEquals(subjectURI.getURI(), statement.getSubject().getURI());
        Assertions.assertEquals(predicateURI.getURI(), statement.getPredicate().getURI());
        Assertions.assertTrue(statement.getObject().isResource());
    }
}
