package edu.upc.essi.dtim.NextiaQR.rewriting;

import com.google.common.collect.Maps;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;

import java.util.List;
import java.util.Map;

public class QueryRewriting implements IQueryRewriting {

    DataLayer dl;
    public QueryRewriting(DataLayer dl) {
        this.dl = dl;
    }

    @Override
    public void generateQueryingStructures(IntegratedGraph IG, List<Dataset> datasetsInQuery) {
        //Here we construct a graph that consists of the global graph, source graphs and mappings graph
        Map<String, LocalGraph> sourceGraphs = Maps.newHashMap();


        List<Map<String,Object>> out = IG.query("SELECT * WHERE { ?s ?p ?o }");
        System.out.println(out);
        System.out.println("XXX");
        //IG.
    }
}
