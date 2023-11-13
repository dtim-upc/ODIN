package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.graphy;

import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary.Vocabulary;
import lombok.Data;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * Represents nodes in a graph used in NextiaGraphy.
 */
@Data
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
        if (!range.equals("")) { // Everything that contains a range is a property
            if (range.contains(XSD.getURI())) {
                type = "datatype";
                if (iriType != null) {
                    if (iriType.equals(Vocabulary.IntegrationDProperty.val())) {
                        isIntegrated = true;
                    }
                }
            } else {
                type = "object";
                if (iriType != null) {
                    if (iriType.equals(Vocabulary.IntegrationOProperty.val())) {
                        isIntegrated = true;
                    }
                }
            }
        } else {
            type = "class";
            if (iriType != null) {
                if (iriType.equals(Vocabulary.IntegrationClass.val())) {
                    isIntegrated = true;
                }
            }
        }
        computeShortType();
    }
}
