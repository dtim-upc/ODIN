package edu.upc.essi.dtim.nextiabs.implementations;

import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataSourceVocabulary;

import java.util.HashMap;
import java.util.Map;

public abstract class DataSource implements IDataSource {
    // Used for the RDFS graph
    public LocalGraph G_target;
    public String wrapper;

    public String id;
    public String name;

    public Map<String, String> prefixes;

    public DataSource(){
        this.G_target = (LocalGraph) CoreGraphFactory.createGraphInstance("local");
        this.id = "";
        this.prefixes = new HashMap<>();
//        setPrefixes();
    }

    public String createIRI(String name){
        if (id.isEmpty()){
            return DataSourceVocabulary.Schema.getURI() + name;
        }
        return DataSourceVocabulary.Schema.getURI() + id + "/" + name;
    }

//    public void setPrefixes(){
//
//        if(id.equals(""))
//            prefixes.put("nextiaSchema", DataSourceVocabulary.Schema.getURI());
//        else
//            prefixes.put("nextiaSchema", DataSourceVocabulary.Schema.getURI()+id+"/");
//        prefixes.put("nextiaDataSource", DataSourceVocabulary.DataSource.getURI() +"/");
//        prefixes.put("rdf", RDF.getUri());
//        prefixes.put("rdfs", RDFS.getURI());
//        prefixes.put("xsd", XSD.getURI());
//        prefixes.put("json", JSON_MM.getURI());
//        prefixes.put("dataFrame", DataFrame_MM.getURI());
//    }
}
