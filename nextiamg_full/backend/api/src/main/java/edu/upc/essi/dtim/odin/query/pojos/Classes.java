package edu.upc.essi.dtim.odin.query.pojos;

public class Classes {

    String iri;
    Boolean isIntegrated;
    String type;

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public Boolean getIntegrated() {
        return isIntegrated;
    }

    public void setIntegrated(Boolean integrated) {
        isIntegrated = integrated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
