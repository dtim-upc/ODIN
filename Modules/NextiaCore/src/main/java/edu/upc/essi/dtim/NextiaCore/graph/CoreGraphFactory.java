package edu.upc.essi.dtim.NextiaCore.graph;


import edu.upc.essi.dtim.NextiaCore.graph.jena.*;

public class CoreGraphFactory {

    public static LocalGraphJenaImpl createLocalGraph() {return new LocalGraphJenaImpl();}
    public static IntegratedGraphJenaImpl createIntegratedGraph() {
        return new IntegratedGraphJenaImpl();
    }
    public static GlobalGraphJenaImpl createGlobalGraph() {
        return new GlobalGraphJenaImpl();
    }
    public static WorkflowGraphJenaImpl createWorkflowGraph() {
        return new WorkflowGraphJenaImpl();
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
        } else if (graphType.equalsIgnoreCase("workflow")) {
            return new WorkflowGraphJenaImpl();
        } else if (graphType.equalsIgnoreCase("normal")) {
            return new GraphJenaImpl();
        } else {
            throw new IllegalArgumentException("Invalid graph type: " + graphType);
        }
    }

}
