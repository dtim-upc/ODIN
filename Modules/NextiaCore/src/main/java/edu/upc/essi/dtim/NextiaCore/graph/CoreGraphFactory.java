package edu.upc.essi.dtim.NextiaCore.graph;


import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;

public class CoreGraphFactory {

    public static LocalGraphJenaImpl createLocalGraph() {
        return new LocalGraphJenaImpl();
    }

    public static IntegratedGraphJenaImpl createIntegratedGraph() {
        return new IntegratedGraphJenaImpl();
    }

    public static GlobalGraphJenaImpl createGlobalGraph() {
        return new GlobalGraphJenaImpl();
    }

    public static GraphJenaImpl createNormalGraph() {
        return new GraphJenaImpl();
    }

    public static Graph createGraphInstance(String graphType) {
        if (graphType.equalsIgnoreCase("local" )) {
            return new LocalGraphJenaImpl();
        } else if (graphType.equalsIgnoreCase("integrated")) {
            return new IntegratedGraphJenaImpl();
        } else if (graphType.equalsIgnoreCase("global")) {
            return new GlobalGraphJenaImpl();
        } else if (graphType.equalsIgnoreCase("normal")) {
            return new GraphJenaImpl();
        } else {
            throw new IllegalArgumentException("Invalid graph type: " + graphType);
        }
    }

}
