package edu.upc.essi.dtim.nextiabs.utils;

import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.XSD;

import java.util.List;
import java.util.Map;


public class DF_MMtoRDFS {

    public Graph productionRulesDataframe_to_RDFS(Graph G_source){
        Graph G_target = CoreGraphFactory.createGraphInstance("local");
        List<Map<String, Object>> result;

        // Rule 1. Instances of J:Object(dataframe) are translated to instances of rdfs:Class .
        result = G_source.query("SELECT ?df ?label WHERE { ?df <"+RDF.type+"> <"+ DataFrame_MM.DataFrame+">. ?df <"+RDFS.label+"> ?label }");
        for(Map<String, Object> res : result){
            G_target.addTriple(res.get("df").toString(), RDF.type, RDFS.Class);
            G_target.addTripleLiteral(res.get("df").toString(), RDFS.label, res.get("label").toString());
            System.out.println("#1 - "+res.get("df").toString()+", "+RDF.type+", "+RDFS.Class);

        }

        // Rule 2. Instances of DF:data (columnes o keys) are translated to instances of rdf:Property .
        result = G_source.query("SELECT ?df ?d ?label WHERE { ?df <"+DataFrame_MM.hasData+"> ?d. ?d <"+RDFS.label+"> ?label   }");
        for(Map<String, Object> res : result) {
            G_target.addTriple(res.get("d").toString(), RDF.type, RDF.Property);
            G_target.addTripleLiteral(res.get("d").toString(), RDFS.label, res.get("label").toString());
            G_target.addTriple(res.get("d").toString(), RDFS.domain, res.get("df").toString());
            System.out.println("#2 - "+res.get("d").toString()+", "+RDF.type+", "+RDF.Property);
            System.out.println("     - "+res.get("d").toString()+", "+RDFS.domain+", "+res.get("df").toString());
        }

        // Rule 3. Array keys (from json) are also ContainerMembershipProperty
        result = G_source.query("SELECT ?df ?d WHERE { ?df <"+DataFrame_MM.hasData+"> ?d . ?d <"+DataFrame_MM.hasDataType+"> ?a . ?a <"+RDF.type+"> <"+DataFrame_MM.Array+"> }");
        for(Map<String, Object> res : result) {
            G_target.addTriple(res.get("d").toString(), RDF.type, RDFS.ContainerMembershipProperty);
            System.out.println("#3 - "+res.get("d").toString()+", "+RDF.type+", "+RDFS.ContainerMembershipProperty);
        }

        //Rule 4. Range of primitives.
        result = G_source.query("SELECT ?d WHERE { ?d <"+DataFrame_MM.hasDataType+"> <"+DataFrame_MM.String+"> . ?d <"+RDF.type+"> <"+DataFrame_MM.Data+"> }");
        for(Map<String, Object> res : result) {
            G_target.addTriple(res.get("d").toString(), RDFS.range, XSD.xstring);
            System.out.println("#4 - "+res.get("d").toString()+", "+RDFS.range+", "+XSD.xstring);
        }
        result = G_source.query("SELECT ?d WHERE { ?d <"+DataFrame_MM.hasDataType+"> <"+DataFrame_MM.Number+"> . ?d <"+RDF.type+"> <"+DataFrame_MM.Data+"> }");
        for(Map<String, Object> res : result) {
            G_target.addTriple(res.get("d").toString(), RDFS.range, XSD.xint);
            System.out.println("#4 - "+res.get("d").toString()+", "+RDFS.range+", "+XSD.xint);
        }

        //Rule 5. Range of dataframes (i.e. json:objects).
        result = G_source.query("SELECT ?d ?dt WHERE { ?d <"+DataFrame_MM.hasDataType+"> ?dt . ?d <"+RDF.type+"> <"+DataFrame_MM.Data+"> . ?dt <"+RDF.type+"> <"+DataFrame_MM.DataFrame+"> }");
        for(Map<String, Object> res : result) {
            G_target.addTriple(res.get("d").toString(), RDFS.range, res.get("dt").toString());
            System.out.println("#5 - "+res.get("d").toString()+", "+RDFS.range+", "+res.get("dt").toString());
        }

        return G_target;
    }
}
