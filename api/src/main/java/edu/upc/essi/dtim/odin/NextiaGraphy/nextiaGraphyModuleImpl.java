package edu.upc.essi.dtim.odin.NextiaGraphy;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import org.apache.commons.lang.StringEscapeUtils;

public class nextiaGraphyModuleImpl implements nextiaGraphyModuleInterface{
    @Override
    public String generateVisualGraph(Graph graph) {
        NextiaGraphy visualLib = new NextiaGraphy();
        return StringEscapeUtils.unescapeJava(visualLib.generateVisualGraphNew(graph));
    }
}
