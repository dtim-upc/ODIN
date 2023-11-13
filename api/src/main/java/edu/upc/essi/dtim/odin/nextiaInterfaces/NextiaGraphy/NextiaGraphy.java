package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy;

import com.google.gson.Gson;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy.Graphy;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy.Links;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy.Nodes;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary.SourceGraph;
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

import static edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary.Namespaces.GLOBALSCHEMA;
import static edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary.Namespaces.SCHEMAINTEGRATION;

/**
 * Class for generating a visual graph representation from RDF data using NextiaGraphy.
 */
public class NextiaGraphy {
    private static final Logger logger = LoggerFactory.getLogger(NextiaGraphy.class);

    /**
     * Generates a visual graph representation from RDF data.
     *
     * @param model The RDF data graph.
     * @return A JSON representation of the visual graph.
     */
    public String generateVisualGraphNew(Graph model) {

        model.write("..\\api\\dbFiles\\ttl" + "/graphJena1.ttl");

        HashMap<String, String> nodesId = new HashMap<>();

        List<Nodes> nodes = new ArrayList<>();

        int conterMember = 1;
        int nodeId = 1;

        // Iterate over subjects in the RDF data graph
        for (Resource r : model.retrieveSubjects().toList()) {
            Nodes n = new Nodes();
            if (r.getURI().contains("pop")) {
                logger.info("here");
            }
            n.setIri(r.getURI());
            n.setId("Class" + nodeId);
            nodeId += 1;

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
                } else if (statement.getPredicate().equals(RDFS.subClassOf) || statement.getPredicate().equals(RDFS.subPropertyOf)) {

                    Nodes property = new Nodes();
                    property.setIri(statement.getPredicate().getURI());
                    property.setType(statement.getPredicate().getURI());
                    property.setDomain(statement.getSubject().toString());
                    nodeId = getNodeId(nodesId, nodes, nodeId, statement, property);
                } else if (statement.getPredicate().equals(RDFS.member)) {
                    memberProperty.setIri(statement.getPredicate().getURI() + "." + conterMember);
                    memberProperty.setLabel("rdfs:member");
                    memberProperty.setIriType(statement.getPredicate().getURI());
                    memberProperty.setShortType("rdfs:member");
                    memberProperty.setType("object");
                    memberProperty.setId("Class" + nodeId);
                    nodeId += 1;
                    nodesId.put(memberProperty.getIri(), memberProperty.getId());
                    memberProperty.setDomain(statement.getSubject().toString());
                    memberProperty.setRange(statement.getObject().toString());
                    conterMember = conterMember + 1;
                } else if (statement.getPredicate().equals(RDFS.label)) {
                    n.setLabel(statement.getObject().toString());
                }
            }
            if (n.getLabel() == null)
                n.setLabel(getLastElem(n.getIri()));
            n.computeType();
            if (memberProperty.getIriType() != null) {
                if (n.getIriType() != null) {
                    if (n.getIriType().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property")) {
                        n.setRange(memberProperty.getRange());
                        n.setType("object");
                    } else {
                        nodes.add(memberProperty);
                    }
                }
            }
            if (n.getIriType() != null)
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
        List<Nodes> nodesReady = nodes.stream().filter(s -> !excluded.contains(s.getIriType()))
                .filter(s -> !s.getIri().contains(GLOBALSCHEMA.val()) && !s.getIri().contains(SCHEMAINTEGRATION.val())
                        && !s.getIri().contains(RDFS.subPropertyOf.toString()) && !s.getRange().equals("http://schema.org/identifier"))
                .collect(Collectors.toList());
        List<Nodes> nodesProperties = nodes.stream()
                .filter(n -> !excludedForProperties.contains(n.getType()) && !n.getIri().contains(RDFS.subPropertyOf.toString())).collect(Collectors.toList());

        List<Links> links = new ArrayList<>();

        int linkid = 1;
        for (Nodes n : nodesProperties) {
            String id = "Link" + linkid;
            linkid += 1;

            Links l = new Links();
            l.setId(id);
            l.setNodeId(n.getId());
            n.setLinkId(id);
            l.setSource(nodesId.get(n.getDomain()));

            if (n.getRange().contains(XSD.getURI())) {
                Nodes datatype = new Nodes();
                datatype.setIri(n.getRange());
                String id2 = "Datatype" + nodeId;
                datatype.setId(id2);
                datatype.setLabel(getLastElem(datatype.getIri()));
                datatype.setXSDDatatype();
                nodeId += 1;
                l.setTarget(id2);
                nodesReady.add(datatype);
                nodes.add(datatype);
            } else {
                l.setTarget(nodesId.get(n.getRange()));
            }
            l.setLabel(n.getLabel());
            if ((l.getSource() == null || l.getTarget() == null) && !n.getRange().equals("http://schema.org/identifier")) {
                logger.error("ERROR " + l.getLabel() + "-------------------------------------");
            }
            if (!n.getRange().equals("http://schema.org/identifier"))
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
        property.setId("Property" + nodeId);
        nodesId.put(property.getIri(), property.getId());
        nodeId += 1;
        property.computeType();
        nodes.add(property);
        return nodeId;
    }

    /**
     * Extracts the last element from an IRI.
     *
     * @param iri The IRI to extract the last element from.
     * @return The last element of the IRI.
     */
    public String getLastElem(String iri) {
        String regex = "/";
        if (iri.contains("#")) {
            regex = "#";
        }
        String[] bits = iri.split(regex);
        String label = bits[bits.length - 1];

        if (label.contains(".")) {
            String[] bits2 = label.split("\\.");
            label = bits2[bits2.length - 1];
        }

        return label;
    }
}
