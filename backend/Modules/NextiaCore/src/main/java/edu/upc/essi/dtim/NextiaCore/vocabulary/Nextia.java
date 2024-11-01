package edu.upc.essi.dtim.NextiaCore.vocabulary;

public enum Nextia {
//    The namespace of the edu.upc.essi.dtim.vocabulary.
    uri("http://www.essi.upc.edu/DTIM/Nextia/"),

    IntegratedClass(uri.val() + "IntegratedClass"),
    IntegratedDatatypeProperty(uri.val() + "IntegratedDatatypeProperty"),
    IntegratedObjectProperty(uri.val() +"IntegratedObjectProperty"),
    DataSource(uri.val() + "DataSource");

    private String element;

    Nextia(String element) {this.element = element;}

    public String val() {
        return element;
    }

}
