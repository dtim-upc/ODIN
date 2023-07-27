package edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary;

public enum Namespaces {

    DTIM("http://www.essi.upc.edu/DTIM"),
    DATASOURCE("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource"),
    PROJECT("http://www.essi.upc.edu/DTIM/NextiaDI/Project"),
    GLOBALSCHEMA("http://www.essi.upc.edu/DTIM/NextiaDI/GlobalSchema"),
    SCHEMAINTEGRATION("http://www.essi.upc.edu/DTIM/NextiaDI/GlobalSchemaComplete"),
    USER("http://www.essi.upc.edu/DTIM/NextiaDI/USER"),
    INTEGRATION("http://www.essi.upc.edu/DTIM/Integration"),
    NEXTIADI("http://www.essi.upc.edu/DTIM/NextiaDI/"),

    S("http://www.essi.upc.edu/dtim/BDIOntology/Source/"),
    G("http://www.essi.upc.edu/dtim/BDIOntology/Global/"),
    I("http://www.essi.upc.edu/dtim/BDIOntology/Integrated/"),
    M("http://www.essi.upc.edu/dtim/BDIOntology/Mappings/"),
    A("http://www.essi.upc.edu/dtim/BDIOntology/Alignments/"),

    SCHEMA("http://schema.org/");

    private String element;

    Namespaces(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }


}
