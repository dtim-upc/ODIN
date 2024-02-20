package edu.upc.essi.dtim.nextiabs.implementations;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataSourceVocabulary;
import edu.upc.essi.dtim.NextiaCore.vocabulary.Formats;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
import edu.upc.essi.dtim.nextiabs.bootstrap.IBootstrap;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapODIN;
import edu.upc.essi.dtim.nextiabs.utils.*;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS.productionRulesDataframe_to_RDFS;

public class JSONBootstrap extends DataSource implements IBootstrap<Graph>, BootstrapODIN {
    // Using DataFrame_MM and without Jena
    protected LocalGraph G_source; //used for the graph in the source metamodel

    public String path;

    protected List<Pair<String,String>> sourceAttributes;
    protected HashMap<String, JSON_Aux> attributesSWJ;
    protected List<String> resourcesLabelSWJ;
    protected List<Pair<String,String>> lateralViews;

    private int ObjectCounter;
    private int ArrayCounter;

    public JSONBootstrap(String id, String name, String path){
        super();
        this.id = id;
        this.name = name;
        this.path = path;
        this.G_source = (LocalGraph) CoreGraphFactory.createGraphInstance("local");
        ObjectCounter = 0;
        ArrayCounter = 0;

        sourceAttributes = Lists.newArrayList();
        attributesSWJ = new HashMap<>();
        resourcesLabelSWJ = new ArrayList<>();
        lateralViews = Lists.newArrayList();
    }

    @Override
    public Graph bootstrapSchema(Boolean generateMetadata) {
//      setPrefixes();
        Document(path,name);
//      G_source.getModel().setNsPrefixes(prefixes);

        //productionRules_JSON_to_RDFS();

        String SELECT = attributesSWJ.entrySet().stream().map( p -> {
            if (p.getKey().equals(p.getValue().getKey())) return p.getValue().getPath() + " AS `" + p.getKey() + "`";
            return  p.getValue().getPath() + " AS `" + p.getValue().getLabel() + "`";
        }).collect(Collectors.joining(", "));


//        System.out.println(" -------------------- ");
//        for (Pair<String, String> s: lateralViews) {
//            System.out.println(s.getLeft() + " ---- " + s.getRight());
//        }

        String LATERAL = lateralViews.stream().map(p -> "LATERAL VIEW explode(" + p.getLeft() + ") AS " + p.getRight()).collect(Collectors.joining("\n"));
        wrapper = "SELECT " + SELECT + " FROM " + name + " " + LATERAL;

        //generateMetadata();

        //G_target.setPrefixes(prefixes);
//        G_source.setPrefixes(prefixes);

        G_source = productionRulesDataframe_to_RDFS(G_source);

        return G_source;
    }

    @Override
    public void generateMetadata() {
        String ds = DataSourceVocabulary.DataSource.getURI() +"/" + name;
        if (!id.isEmpty()){
            ds = DataSourceVocabulary.DataSource.getURI() +"/" + id;
            G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_ID.getURI(), id);
        }
        G_source.addTriple( ds , RDF.type,  DataSourceVocabulary.DataSource.getURI() );
        G_source.addTripleLiteral( ds , DataSourceVocabulary.HAS_PATH.getURI(), path);
        G_source.addTripleLiteral( ds , RDFS.label,  name );

        G_target.addTriple( ds , RDF.type,  DataSourceVocabulary.DataSource.getURI() );
        G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_PATH.getURI(), path);
        G_target.addTripleLiteral( ds , RDFS.label,  name );

        G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_FORMAT.getURI(), Formats.JSON.val());
        G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.getURI(), wrapper);
    }

    @Override
    public Graph bootstrapSchema() {
        return bootstrapSchema(false);
    }

    @Override
    public BootstrapResult bootstrapDataset(Dataset dataset) {
        bootstrapSchema();
        return new BootstrapResult(G_source, wrapper);
    }


    private void Document(String path, String D) {
        InputStream fis;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found");
        }

        G_source.addTriple(createIRI(D), RDF.type, DataFrame_MM.DataSource);
        Object(Json.createReader(fis).readValue().asJsonObject(),new JSON_Aux(D,"",""));

    }

    private void DataType(JsonValue D, JSON_Aux p) {
        if (D.getValueType() == JsonValue.ValueType.OBJECT) Object((JsonObject)D, p);
        else if (D.getValueType() == JsonValue.ValueType.ARRAY) Array((JsonArray)D, p);
        else Primitive(D,p);
    }

    private void Object (JsonObject D, JSON_Aux p) {
        String u_prime = freshObject();
        String iri_u_prime = createIRI(u_prime);
        G_source.addTriple(iri_u_prime,RDF.type,DataFrame_MM.DataFrame);
        G_source.addTripleLiteral(iri_u_prime, RDFS.label, p.getKey());
        D.forEach((k,v)-> {
            String k_prime = freshAttribute(k);
            resourcesLabelSWJ.add(k_prime);
            String iri_k = createIRI( k_prime );
            G_source.addTriple(iri_k, RDF.type,DataFrame_MM.Data);
            if (v.getValueType() == JsonValue.ValueType.OBJECT || v.getValueType() == JsonValue.ValueType.ARRAY) {
                G_source.addTripleLiteral(iri_k, RDFS.label, "has_" + k_prime);
            }
            else {
                G_source.addTripleLiteral(iri_k, RDFS.label, k_prime);
            }
            G_source.addTriple(iri_u_prime,DataFrame_MM.hasData,iri_k);
            String path_tmp = p.getPath() + "." + k;
            if (p.getPath().isEmpty()) {
                path_tmp = k;
            }

            DataType(v, new JSON_Aux(k, k_prime, path_tmp));
        });
        G_source.addTriple(createIRI(p.getLabel()),DataFrame_MM.hasDataType, iri_u_prime);
    }

    private String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length());
        } else {
            return string;
        }
    }

    private void Array (JsonArray D, JSON_Aux p) {
        String u_prime = freshArray();
        String iri_u_prime = createIRI(u_prime);
        G_source.addTriple(iri_u_prime,RDF.type,DataFrame_MM.Array);
        G_source.addTripleLiteral(iri_u_prime, RDFS.label, p.getKey());
        if (!D.isEmpty()) {
            DataType(D.get(0), new JSON_Aux(  u_prime, p.getLabel() ,  replaceLast(p.getPath(),p.getKey(), p.getKey()+"_view"  )  ));
        } else {
            // TODO: some ds have empty array, check below example images array
            G_source.addTriple(createIRI(p.getKey()),DataFrame_MM.hasDataType,DataFrame_MM.String);
        }
        lateralViews.add(Pair.of(p.getKey(), p.getKey()+"_view"));
        G_source.addTriple(createIRI(p.getKey()),DataFrame_MM.hasDataType,iri_u_prime);
//		G_source.add(createIRI(u_prime),JSON_MM.hasMember,createIRI(p));
    }

    private void Primitive(JsonValue D, JSON_Aux p) {
        resourcesLabelSWJ.add(p.getLabel());
        if (D.getValueType() == JsonValue.ValueType.NUMBER) {
            G_source.addTriple(createIRI(p.getLabel()),DataFrame_MM.hasDataType,DataFrame_MM.Number);
            attributesSWJ.put(p.getLabel(),p);
        }
        // Boolean does not exist in the library
        //else if (D.getValueType() == JsonValue.ValueType.BOOLEAN) {
        //			G_source.add(createIRI(p),JSON_MM.hasValue,JSON_MM.Boolean);
        //		}
        else {
            G_source.addTriple(createIRI(p.getLabel()),DataFrame_MM.hasDataType,DataFrame_MM.String);
            attributesSWJ.put(p.getLabel(),p);
        }
    }

    private void instantiateMetamodel() {
        G_source.addTriple(DataFrame_MM.Number, RDF.type, DataFrame_MM.Primitive);
        G_source.addTriple(DataFrame_MM.String, RDF.type, DataFrame_MM.Primitive);
        //G_source.add(JSON_MM.Boolean.getURI(), RDF.type, JSON_MM.Primitive);
    }

    private String freshObject() {
        ObjectCounter = ObjectCounter + 1;
        return "Object_" + ObjectCounter;
    }

    private String freshAttribute( String attribute ) {
        String att = attribute;
        int cont = 2;
        while(attributesSWJ.containsKey(att) || resourcesLabelSWJ.contains(att)) {
            att = attribute + cont;
            cont = cont +1;
        }
        return att;
    }

    private String freshArray() {
        ArrayCounter = ArrayCounter + 1;
        return "Array_" + ArrayCounter;
    }
}




//    private void productionRules_JSON_to_RDFS() {
//        // Rule 1. Instances of J:Object are translated to instances of rdfs:Class .
//        G_source.runAQuery("SELECT ?o ?label WHERE { ?o <"+RDF.type+"> <"+JSON_MM.Object+">. ?o <"+RDFS.label+"> ?label }").forEachRemaining(res -> {
//            G_target.add(res.getResource("o").getURI(),RDF.type,RDFS.Class);
//            G_target.addLiteral(res.getResource("o").getURI(),RDFS.label, res.getLiteral("label") );
//            System.out.println("#1 - "+res.getResource("o").getURI()+", "+RDF.type+", "+RDFS.Class);
//        });
//
//        // Rule 2. Instances of J:Array are translated to instances of rdfs:Class and rdf:Seq .
////		G_source.runAQuery("SELECT ?a WHERE { ?a <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
////			G_target.add(res.getResource("a").getURI(),RDF.type,RDFS.Class); System.out.println("#2 - "+res.getResource("a").getURI()+", "+RDF.type+", "+RDFS.Class);
////			G_target.add(res.getResource("a").getURI(),RDF.type,RDF.Seq); System.out.println("#2 - "+res.getResource("a").getURI()+", "+RDF.type+", "+RDF.Seq);
////		});
//
//        // Rule 2. Instances of J:Key are translated to instances of rdf:Property . Additionally, this requires defining the rdfs:domain
//        //of such newly defined instance of rdf:Property .
//        G_source.runAQuery("SELECT ?o ?k ?label WHERE { ?o <"+JSON_MM.hasKey+"> ?k. ?k <"+RDFS.label+"> ?label   }").forEachRemaining(res -> {
//            G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#3 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
//            G_target.addLiteral(res.getResource("k").getURI(),RDFS.label, res.getLiteral("label") );
//            G_target.add(res.getResource("k").getURI(),RDFS.domain,res.getResource("o").getURI()); System.out.println("#3 - "+res.getResource("k").getURI()+", "+RDFS.domain+", "+res.getResource("o").getURI());
//        });
//
//        // Rule 3. Array keys are also ContainerMembershipProperty
//        G_source.runAQuery("SELECT ?o ?k WHERE { ?o <"+JSON_MM.hasKey+"> ?k . ?k <"+JSON_MM.hasValue+"> ?a . ?a <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
//            G_target.add(res.getResource("k").getURI(),RDF.type,RDFS.ContainerMembershipProperty);
//        });
//
//        //Rule 4. Range of primitives.
//        G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+">+ <"+JSON_MM.String+"> . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> }").forEachRemaining(res -> {
////			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
//            G_target.add(res.getResource("k").getURI(),RDFS.range, XSD.xstring); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+XSD.xstring);
//        });
//        G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+">+ <"+JSON_MM.Number+"> . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> }").forEachRemaining(res -> {
////			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
//            G_target.add(res.getResource("k").getURI(),RDFS.range,XSD.xint); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+XSD.xint);
//        });
//
//        //Rule 5. Range of objects.
//        G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+">+ ?v . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> . ?v <"+RDF.type+"> <"+JSON_MM.Object+"> }").forEachRemaining(res -> {
////			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
//            G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("v")); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+res.getResource("v"));
//        });

//6- Range of arrays (objects)
//		G_source.runAQuery("SELECT ?k ?x WHERE { ?k <"+JSON_MM.hasValue+"> ?v . ?v <"+JSON_MM.hasValue+">+ ?x . " +
//				"?k <"+RDF.type+"> <"+JSON_MM.Key+"> . ?v <"+RDF.type+"> <"+JSON_MM.Array+"> . " +
//				"?x <"+RDF.type+"> <"+JSON_MM.Object+">}").forEachRemaining(res -> {
//			G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("x")); System.out.println("#6 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+res.getResource("v"));
//		});

/**
 // Array of primitives

 // Array of arrays


 G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> ?v . ?v <"+RDF.type+"> <"+JSON_MM.Array+"> . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> }").forEachRemaining(res -> {
 //			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
 //?			G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("v")); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+res.getResource("v"));
 });

 //Arrays of objects


 //Rule 6. Instances of J:Primitive which are members of an instance of J:Array are connected to its corresponding
 //counterpart in the xsd vocabulary using the rdfs:member property. We show the case for instances of J:String whose
 //counterpart is xsd:string . The procedure for instances of J:Number and J:Boolean is similar using their pertaining type.
 G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasValue+"> <"+JSON_MM.String+"> . ?a <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
 //G_target.add(XSD.xstring.getURI(),RDFS.member,res.getResource("a").getURI());
 G_target.add(res.getResource("a").getURI(),RDFS.member,XSD.xstring); System.out.println("#6 - "+res.getResource("a").getURI()+", "+RDFS.member+", "+XSD.xstring);
 });
 //G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasValue+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.Number+"> }").forEachRemaining(res -> {
 //	G_target.add(XSD.xint.getURI(),RDFS.member,res.getResource("a").getURI());
 //});

 //Rule 7. Instances of J:Object or J:Array which are members of an instance of J:Array are connected via the rdfs:member
 //property.
 G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> ?a . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> . ?a <"+RDF.type+"> <"+JSON_MM.Array+"> " +
 ". ?a <"+JSON_MM.hasValue+"> ?v }").forEachRemaining(res -> {
 G_target.add(res.getResource("k").getURI(),RDFS.member,res.getResource("v")); System.out.println("#7 - "+res.getResource("k").getURI()+", "+RDFS.member+", "+res.getResource("v"));
 });
 //		G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasValue+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
 //			G_target.add(res.getResource("a").getURI(),RDFS.member,res.getResource("d")); System.out.println("#7 - "+res.getResource("a").getURI()+", "+RDFS.member+", "+res.getResource("d"));
 //		});
 **/
//    }
