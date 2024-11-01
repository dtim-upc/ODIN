package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy;

import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary.Vocabulary;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * Represents nodes in a graph used in NextiaGraphy.
 */
public class Nodes {
    String id;          // Unique identifier for the node
    String iri;         // IRI (Internationalized Resource Identifier) of the node
    String iriType;     // Type of the IRI
    String shortType;   // Shortened type of the IRI
    String type;        // Type of the node (class, object, or datatype)
    String label;       // Label associated with the node
    String domain;      // Domain of the node (for properties)
    String range;       // Range of the node (for properties)
    Boolean isIntegrated; // Indicates if the node is integrated (true/false)

    // Optional
    String linkId;      // Identifier of a linked node (optional)

    /**
     * Default constructor for Nodes.
     */
    public Nodes() {
        this.domain = "";
        this.range = "";
        this.isIntegrated = false;
    }

    /**
     * Sets the node type to "xsdType" and the short type to "xsd:String".
     */
    public void setXSDDatatype() {
        this.type = "xsdType";
        this.shortType = "xsd:String";
    }

    /**
     * Computes the short type based on the IRI type.
     */
    public void computeShortType() {
        // Check if the IRI type is not null
        // this if it's because rdfs:subclass does not have a type...
        if (iriType != null) {
            if (iriType.contains(RDFS.getURI())) {
                this.shortType = "rdfs:" + iriType.replace(RDFS.getURI(), "");
            } else if (iriType.contains(RDF.getURI())) {
                this.shortType = "rdf:" + iriType.replace(RDF.getURI(), "");
            } else if (iriType.contains("http://www.essi.upc.edu/DTIM/NextiaDI/")) {
                this.shortType = "nextia:" + iriType.replace("http://www.essi.upc.edu/DTIM/NextiaDI/", "");
            } else {
                this.shortType = iriType;
            }
        }
    }

    /**
     * Computes the type of the node based on its range and IRI type.
     */
    public void computeType() {
        if (!range.isEmpty()) { // Everything that contains a range is a property
            if (range.contains(XSD.getURI())) {
                type = "datatype";
                if (iriType != null) {
                    if (iriType.equals(Vocabulary.IntegrationDProperty.getElement())) {
                        isIntegrated = true;
                    }
                }
            } else {
                type = "object";
                if (iriType != null) {
                    if (iriType.equals(Vocabulary.IntegrationOProperty.getElement())) {
                        isIntegrated = true;
                    }
                }
            }
        } else {
            type = "class";
            if (iriType != null) {
                if (iriType.equals(Vocabulary.IntegrationClass.getElement())) {
                    isIntegrated = true;
                }
            }
        }
        computeShortType();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }
    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getIriType() {
        return iriType;
    }
    public void setIriType(String iriType) {
        this.iriType = iriType;
    }

    public String getShortType() {
        return shortType;
    }
    public void setShortType(String shortType) {
        this.shortType = shortType;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRange() {
        return range;
    }
    public void setRange(String range) {
        this.range = range;
    }

    public Boolean getIntegrated() {
        return isIntegrated;
    }
    public void setIntegrated(Boolean integrated) {
        isIntegrated = integrated;
    }

    public String getLinkId() {
        return linkId;
    }
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }
}
