package edu.upc.essi.dtim.nextiabs.temp;

import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import org.apache.jena.atlas.lib.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintGraph {
    public static void printGraph(Graph g){
        HashMap<String, List<Pair<String, String>>> results = new HashMap<>();
        List<Map<String, Object>> result = g.query("SELECT ?s ?p ?o WHERE { ?s ?p ?o } ");

        for(Map<String, Object> t : result){

            if(results.get(prefixed(t.get("s").toString())) == null){
                ArrayList<Pair<String, String>> l = new ArrayList<>();
                results.put(prefixed(t.get("s").toString()), l);
            }

            Pair p = new Pair(prefixed(t.get("p").toString()), prefixed(t.get("o").toString()));
            results.get(prefixed(t.get("s").toString())).add(p);
        }
        for (Map.Entry<String, List<Pair<String, String>>> entry : results.entrySet()) {
            String key = entry.getKey();
            System.out.println(key);
            for (Pair<String, String> p : results.get(key)){
                System.out.println("      " + p.getLeft() + " --> " + p.getRight());
            }
        }
    }
    private static String prefixed(String inputURI) {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/", "DTIM:");
        prefixes.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF:");
        prefixes.put("http://www.w3.org/2000/01/rdf-schema#", "RDFS:");
        prefixes.put("https://www.essi.upc.edu/dtim/dataframe-metamodel#", "METAMODEL:");
        prefixes.put("http://www.w3.org/2001/XMLSchema#", "XSD:");
//        String outputURI = inputURI;
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            inputURI = inputURI.replace(key, value);
        }
        return inputURI;
    }
}
