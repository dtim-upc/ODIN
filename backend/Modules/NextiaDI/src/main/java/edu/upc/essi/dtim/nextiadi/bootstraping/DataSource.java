package edu.upc.essi.dtim.nextiadi.bootstraping;

import edu.upc.essi.dtim.nextiadi.bootstraping.metamodels.JSON_MM;
import edu.upc.essi.dtim.nextiadi.bootstraping.utils.JSON_Aux;
import edu.upc.essi.dtim.nextiadi.config.DataSourceVocabulary;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataSource {

    protected Graph G_source; //used for the graph in the source metamodel
    protected Graph G_target; //used for the RDFS graph

    protected String wrapper;
    //List of pairs, where left is the IRI in the graph and right is the attribute in the wrapper (this will create sameAs edges)
    protected List<Pair<String,String>> sourceAttributes;

    //TODO: these attributesSWJ and resourcesLabelSWJ should be merged somehow...or optimize the code
    protected HashMap<String, JSON_Aux> attributesSWJ;
    protected List<String> resourcesLabelSWJ;

    @Deprecated
    protected List<Pair<String,String>> attributes;
    protected List<Pair<String,String>> lateralViews;
    protected String id = "";

    protected Map<String, String> prefixes;

    DataSource() {
        init();
    }

    protected void setPrefixes(){
        prefixes.put("nextiaSchema", DataSourceVocabulary.Schema.val());
        prefixes.put("nextiaDataSource", DataSourceVocabulary.DataSource.val() +"/");
        prefixes.put("rdf", RDF.getURI());
        prefixes.put("rdfs", RDFS.getURI());
        prefixes.put("xsd", XSD.getURI());
        prefixes.put("json", JSON_MM.getURI());
    }

    protected void setPrefixesID(String id){
        prefixes.put("nextiaSchema", DataSourceVocabulary.Schema.val()+id+"/");
    }

    protected void init(){
        G_source = new Graph();
        G_target = new Graph();

        prefixes = new HashMap<>();
        setPrefixes();
        sourceAttributes = Lists.newArrayList();

        attributes = Lists.newArrayList();
        attributesSWJ = new HashMap<>();
        resourcesLabelSWJ = new ArrayList<>();
        lateralViews = Lists.newArrayList();
        id = "";
    }

    public void addBasicMetaData(String name, String path, String ds){
        G_source.add( ds , RDF.type.getURI(),  DataSourceVocabulary.DataSource.val() );
        G_source.addLiteral( ds , DataSourceVocabulary.HAS_PATH.val(), path);
        G_source.addLiteral( ds , RDFS.label.getURI(),  name );

        G_target.add( ds , RDF.type.getURI(),  DataSourceVocabulary.DataSource.val() );
        G_target.addLiteral( ds , DataSourceVocabulary.HAS_PATH.val(), path);
        G_target.addLiteral( ds , RDFS.label.getURI(),  name );
    }

    protected String createIRI(String name){
        if(id.equals("")){
            return DataSourceVocabulary.Schema.val() + name;
        }
        return DataSourceVocabulary.Schema.val() + id+"/"+ name;
    }
}
