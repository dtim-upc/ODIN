package edu.upc.essi.dtim.odin.NextiaGraphy;

import com.google.gson.Gson;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.odin.NextiaGraphy.graphy.Graphy;
import edu.upc.essi.dtim.odin.NextiaGraphy.graphy.Links;
import edu.upc.essi.dtim.odin.NextiaGraphy.graphy.Nodes;
import edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary.SourceGraph;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary.Namespaces.GLOBALSCHEMA;
import static edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary.Namespaces.SCHEMAINTEGRATION;

public class NextiaGraphy {
    private static final Logger logger = LoggerFactory.getLogger(NextiaGraphy.class);
    //cities csv ----------------- {"nodes":[{"id":"Class1","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/LonM","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"LonM","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link1"},{"id":"Class2","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/NS","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"NS","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link2"},{"id":"Class3","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/LatS","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"LatS","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link3"},{"id":"Class4","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/LatD","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"LatD","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link4"},{"id":"Class5","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/LonD","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"LonD","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link5"},{"id":"Class6","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/City","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"City","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link6"},{"id":"Class7","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","iriType":"http://www.w3.org/2000/01/rdf-schema#Class","shortType":"rdfs:Class","type":"class","label":"cities csv","domain":"","range":"","isIntegrated":false},{"id":"Class8","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/State","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"State","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link7"},{"id":"Class9","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/EW","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"EW","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link8"},{"id":"Class11","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/LonS","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"LonS","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link9"},{"id":"Class12","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/LatM","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"LatM","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1651/cities csv","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link10"},{"id":"Datatype13","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype14","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype15","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype16","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype17","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype18","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype19","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype20","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype21","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype22","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false}],"links":[{"id":"Link1","nodeId":"Class1","source":"Class7","target":"Datatype13","label":"LonM"},{"id":"Link2","nodeId":"Class2","source":"Class7","target":"Datatype14","label":"NS"},{"id":"Link3","nodeId":"Class3","source":"Class7","target":"Datatype15","label":"LatS"},{"id":"Link4","nodeId":"Class4","source":"Class7","target":"Datatype16","label":"LatD"},{"id":"Link5","nodeId":"Class5","source":"Class7","target":"Datatype17","label":"LonD"},{"id":"Link6","nodeId":"Class6","source":"Class7","target":"Datatype18","label":"City"},{"id":"Link7","nodeId":"Class8","source":"Class7","target":"Datatype19","label":"State"},{"id":"Link8","nodeId":"Class9","source":"Class7","target":"Datatype20","label":"EW"},{"id":"Link9","nodeId":"Class11","source":"Class7","target":"Datatype21","label":"LonS"},{"id":"Link10","nodeId":"Class12","source":"Class7","target":"Datatype22","label":"LatM"}]}
    //cars json ----------------- {"nodes":[{"id":"Class1","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/Object_1","iriType":"http://www.w3.org/2000/01/rdf-schema#Class","shortType":"rdfs:Class","type":"class","label":"json d","domain":"","range":"","isIntegrated":false},{"id":"Class3","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/Object_2","iriType":"http://www.w3.org/2000/01/rdf-schema#Class","shortType":"rdfs:Class","type":"class","label":"Array_1","domain":"","range":"","isIntegrated":false},{"id":"Class4","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/nombre","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"nombre","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/Object_2","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link1"},{"id":"Class5","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/modelos","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"has modelos","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/Object_2","range":"http://www.w3.org/2001/XMLSchema#string","isIntegrated":false,"linkId":"Link2"},{"id":"Class6","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/marcas","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"object","label":"has marcas","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/Object_1","range":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/Object_2","isIntegrated":false,"linkId":"Link3"},{"id":"Class7","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/id","iriType":"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property","shortType":"rdf:Property","type":"datatype","label":"id","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/1653/Object_2","range":"http://www.w3.org/2001/XMLSchema#int","isIntegrated":false,"linkId":"Link4"},{"id":"Datatype8","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype9","iri":"http://www.w3.org/2001/XMLSchema#string","shortType":"xsd:String","type":"xsdType","label":"string","domain":"","range":"","isIntegrated":false},{"id":"Datatype10","iri":"http://www.w3.org/2001/XMLSchema#int","shortType":"xsd:String","type":"xsdType","label":"int","domain":"","range":"","isIntegrated":false}],"links":[{"id":"Link1","nodeId":"Class4","source":"Class3","target":"Datatype8","label":"nombre"},{"id":"Link2","nodeId":"Class5","source":"Class3","target":"Datatype9","label":"has modelos"},{"id":"Link3","nodeId":"Class6","source":"Class1","target":"Class3","label":"has marcas"},{"id":"Link4","nodeId":"Class7","source":"Class3","target":"Datatype10","label":"id"}]}

    public String generateVisualGraphNew(Graph model){

        model.write("..\\api\\dbFiles\\ttl"+"/graphJena1.ttl");

        HashMap<String, String> nodesId = new HashMap<>();

        List<Nodes> nodes = new ArrayList<>();

        int conterMember = 1;
        // create nodes
        int nodeId = 1;
        for( Resource r : model.retrieveSubjects().toList() ) {

            Nodes n = new Nodes();
            if(r.getURI().contains("pop")) {
                logger.info("here");
            }
            n.setIri(r.getURI());
            n.setId("Class"+nodeId);
            nodeId += 1 ;

            nodesId.put(n.getIri(), n.getId());

            Nodes memberProperty = new Nodes();
            for (Statement statement : r.listProperties().toList()) {

                if (statement.getPredicate().equals(RDF.type)) {
                    n.setIriType(statement.getObject().toString());
                } else if (statement.getPredicate().equals(RDFS.domain)) {
                    n.setDomain(statement.getObject().toString());
                } else if (statement.getPredicate().equals(RDFS.range)) {
                    n.setRange(statement.getObject().toString());
                } else if (statement.getPredicate().getURI().equals(SourceGraph.HAS_ATTRIBUTE.val()) || statement.getPredicate().getURI().equals(SourceGraph.HAS_WRAPPER.val())) {

                    Nodes property = new Nodes();
                    property.setIri(statement.getPredicate().getURI());
                    property.setType(statement.getPredicate().getURI());
                    property.setDomain(n.getIri());
                    nodeId = getNodeId(nodesId, nodes, nodeId, statement, property);
                } else if(statement.getPredicate().equals(RDFS.subClassOf)  || statement.getPredicate().equals(RDFS.subPropertyOf)

                ) {

                    Nodes property = new Nodes();
                    property.setIri(statement.getPredicate().getURI());
                    property.setType(statement.getPredicate().getURI());
                    property.setDomain(statement.getSubject().toString());
                    nodeId = getNodeId(nodesId, nodes, nodeId, statement, property);
                } else if(statement.getPredicate().equals(RDFS.member)){


                    memberProperty.setIri(statement.getPredicate().getURI()+"."+conterMember);
                    memberProperty.setLabel("rdfs:member");
                    memberProperty.setIriType(statement.getPredicate().getURI());
                    memberProperty.setShortType("rdfs:member");
                    memberProperty.setType("object");
                    //todo: not sure if id should be class or property
                    memberProperty.setId("Class"+nodeId);
                    nodeId += 1 ;
                    nodesId.put(memberProperty.getIri(), memberProperty.getId());
                    memberProperty.setDomain(statement.getSubject().toString());
                    memberProperty.setRange(statement.getObject().toString());

                    conterMember = conterMember+1;

                } else if( statement.getPredicate().equals(RDFS.label) ){
                    n.setLabel( statement.getObject().toString() );
                }
            }
            if(n.getLabel() == null)
                n.setLabel(getLastElem(n.getIri()));
            n.computeType();
            if(memberProperty.getIriType() != null) {

                if( n.getIriType() != null) {
                    if (n.getIriType().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property")) {

                        n.setRange(memberProperty.getRange());
                        n.setType("object");

                    } else {
                        nodes.add(memberProperty);
                    }
                }

            }

            if(n.getIriType() != null)
                nodes.add(n);

        }

        List<String> excluded = new ArrayList<>();
        excluded.add("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource");
        excluded.add(SCHEMAINTEGRATION.val());
        excluded.add(GLOBALSCHEMA.val());

        List<String> excludedForProperties = new ArrayList<>();
        excludedForProperties.add("class");
        excludedForProperties.add("integratedClass");


        System.out.println(nodes.size());
        List<Nodes> nodesReady = nodes.stream().filter( s -> !excluded.contains(s.getIriType()) )
                .filter( s -> !s.getIri().contains(GLOBALSCHEMA.val()) && !s.getIri().contains(SCHEMAINTEGRATION.val())
                        && !s.getIri().contains(RDFS.subPropertyOf.toString()) && !s.getRange().equals("http://schema.org/identifier") )
                .collect(Collectors.toList());
        List<Nodes> nodesProperties = nodes.stream()
                .filter(n -> !excludedForProperties.contains(n.getType()) && !n.getIri().contains(RDFS.subPropertyOf.toString())  ).collect(Collectors.toList());

        List<Links> links = new ArrayList<>();


        int linkid =1;
        for (Nodes n: nodesProperties) {

            String id = "Link"+linkid;
            linkid +=1;

            Links l = new Links();
            l.setId(id);
            l.setNodeId(n.getId());
            n.setLinkId(id);
            l.setSource(nodesId.get(n.getDomain()));

            if(n.getRange().contains(XSD.getURI() )) {
                Nodes datatype = new Nodes();
                datatype.setIri(n.getRange());
                String id2 = "Datatype"+nodeId;
                datatype.setId(id2);
                datatype.setLabel(getLastElem(datatype.getIri()));
                datatype.setXSDDatatype();
                nodeId +=1;

                l.setTarget(id2);
                nodesReady.add(datatype);
                nodes.add(datatype);
            } else {
                l.setTarget(nodesId.get(n.getRange()));
            }
            l.setLabel(n.getLabel());
            if( (l.getSource() == null || l.getTarget() == null  ) && !n.getRange().equals("http://schema.org/identifier") ){
                logger.error("ERROR " + l.getLabel()+"-------------------------------------");
            }
            // TODO: support to see subproperty of....it implies to connect to properties visually
            if(!n.getRange().equals("http://schema.org/identifier"))
                links.add(l);
        }


        Graphy gr = new Graphy();
        gr.setNodes(nodesReady);
        gr.setLinks(links);
        System.out.println("----------------- " + new Gson().toJson(gr));
        return new Gson().toJson(gr);
    }

    private int getNodeId(HashMap<String, String> nodesId, List<Nodes> nodes, int nodeId, Statement statement, Nodes property) {
        property.setRange(statement.getObject().toString());
        property.setLabel(getLastElem(property.getIri()));
        property.setId("Property"+nodeId);
        nodesId.put(property.getIri(), property.getId());
        nodeId += 1 ;
        property.computeType();
        nodes.add(property);
        return nodeId;
    }

    public String getLastElem(String iri) {
        String regex = "/";
        if(iri.contains("#")){
            regex = "#";
        }
        String[] bits = iri.split(regex);
        String label = bits[bits.length - 1]; // it could throw an exception when split empty....CHECK!

        if(label.contains(".")){
            String[] bits2 = label.split("\\.");
            label = bits2[bits2.length - 1];
        }

        return label;
    }
}
