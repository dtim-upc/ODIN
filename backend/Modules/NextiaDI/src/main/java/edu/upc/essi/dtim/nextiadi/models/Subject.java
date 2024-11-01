package edu.upc.essi.dtim.nextiadi.models;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;

public class Subject {

    String iri;
    String type;
    String domain;
    String range;

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getPropertyType() {

        if(range != null) {
            if(range.contains(XSD.getURI()) ){
                return OWL.DatatypeProperty.getURI();
            }
            return OWL.ObjectProperty.getURI();
        }
        return null;

    }


}
