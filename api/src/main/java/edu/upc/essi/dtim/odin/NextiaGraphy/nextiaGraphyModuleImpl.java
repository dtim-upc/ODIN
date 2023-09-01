package edu.upc.essi.dtim.odin.NextiaGraphy;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Implementation of the nextiaGraphyModuleInterface for generating visual graphs.
 */
public class nextiaGraphyModuleImpl implements nextiaGraphyModuleInterface {

    /**
     * Generates a visual graph representation from an RDF graph.
     *
     * @param graph The RDF graph to generate the visual graph from.
     * @return A JSON representation of the visual graph (escaped as a Java string).
     */
    @Override
    public String generateVisualGraph(Graph graph) {
        // Create an instance of NextiaGraphy
        NextiaGraphy visualLib = new NextiaGraphy();

        // Generate the visual graph using the NextiaGraphy instance
        String visualGraphJson = visualLib.generateVisualGraphNew(graph);

        // Escape special characters in the JSON and return it as a Java string
        return StringEscapeUtils.unescapeJava(visualGraphJson);
    }
}
