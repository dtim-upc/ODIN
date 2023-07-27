package edu.upc.essi.dtim.odin.NextiaGraphy.graphy;

import edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary.Vocabulary;
import lombok.Data;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;


@Data
public class Nodes {

    String id;
    String iri;
    String iriType;
    String shortType;
    String type;
    String label;
    String domain;
    String range;
    Boolean isIntegrated;

    // optional
    String linkId;

    public Nodes(){
        this.domain = "";
        this.range = "";
        this.isIntegrated = false;
    }

    public void setXSDDatatype(){
        this.type = "xsdType";
        this.shortType = "xsd:String";
    }

    public void computeShortType(){
//        this if it's because rdfs:subclass does not have a type...
        if(iriType != null) {
            if(iriType.contains(RDFS.getURI())){
                this.shortType = "rdfs:"+ iriType.replace(RDFS.getURI(),"");
            } else if (iriType.contains(RDF.getURI())) {
                this.shortType = "rdf:"+ iriType.replace(RDF.getURI(),"");
            } else if (iriType.contains("http://www.essi.upc.edu/DTIM/NextiaDI/")) {
                this.shortType = "nextia:" + iriType.replace("http://www.essi.upc.edu/DTIM/NextiaDI/","");
            } else {
                this.shortType = iriType;
            }
        }

//        this.shortType = ""
    }

    public void computeType() {

        // think better way to do this

//        if(iriType.equals(RDFS.Class.getURI())){
//            type = "class";
//        }
        if(!range.equals("") )     { // everything that contains range is property
            if( range.contains(XSD.getURI())) {
//                type = "datatypeProperty";

//                TODO: set in Nextiadi a variable that can be used in external
                type = "datatype";
                if(iriType != null)
                    if (iriType.equals(Vocabulary.IntegrationDProperty.val())) {
                        isIntegrated = true;
//                                "integratedDatatypeProperty";
                    }


//            } else if ( iriType.equals(RDF.Property.getURI())  ) {
//                type = "objectProperty";
            } else {
                type = "object";
                if(iriType != null)
                    if (iriType.equals(Vocabulary.IntegrationOProperty.val())) {
                        isIntegrated = true;
//                                "integratedObjectProperty";
                    }

//                type = "objectProperty"; // missing subclassof and subpropertyof

            }
        } else {
            type = "class";
            if(iriType != null) {
                if (iriType.equals(Vocabulary.IntegrationClass.val())) {
//                    type = "integratedClass";
                    isIntegrated = true;
                }
            }



        }
        computeShortType();


    }

}
