package edu.upc.essi.dtim.odin.query.pojos;

public class Property {

    String domain;
    String range;
    String iri;
    Boolean isIntegrated;
    String type;

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

    public boolean isDataTypeProperty(){

        if(range.contains("http://www.w3.org/2001/XMLSchema#")){
            return true;
        }
        return false;

    }


}
