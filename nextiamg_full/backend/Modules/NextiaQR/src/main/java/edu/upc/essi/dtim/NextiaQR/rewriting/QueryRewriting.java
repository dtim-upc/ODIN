package edu.upc.essi.dtim.NextiaQR.rewriting;

import com.google.common.collect.Maps;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.GlobalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;

import java.util.List;
import java.util.Map;

public class QueryRewriting implements IQueryRewriting {

    DataLayer dl;
    public QueryRewriting(DataLayer dl) {
        this.dl = dl;
    }

    @Override
    public void generateQueryingStructures(IntegratedGraphJenaImpl IG, List<Dataset> datasetsInQuery) {
        //The source graphs are available in the list of datasets
        Map<String, LocalGraph> sourceGraphs = Maps.newHashMap();
        datasetsInQuery.iterator().forEachRemaining(d -> sourceGraphs.put(d.getLocalGraph().getGraphName(),d.getLocalGraph()));
        Map<String, GlobalGraph> globalGraph = Maps.newHashMap();
        globalGraph.put("G",IG.getGlobalGraph());

        //Compute subgraphs

        List<Map<String,Object>> out = IG.query("SELECT * WHERE { ?s ?p ?o }");
        System.out.println(out);
        System.out.println("XXX");
        //IG.
    }

}
