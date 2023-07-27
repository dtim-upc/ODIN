package edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary;

public enum Vocabulary {


    IntegrationClass(Namespaces.NEXTIADI.val() + "IntegratedClass"),
    IntegrationDProperty( Namespaces.NEXTIADI.val() + "IntegratedDatatypeProperty"),
    IntegrationOProperty( Namespaces.NEXTIADI.val() + "IntegratedObjectProperty"),

    JoinProperty(Namespaces.NEXTIADI.val() + "JoinProperty"),
    JoinObjectProperty(Namespaces.NEXTIADI.val() + "JoinObjectProperty");

    private String element;

    Vocabulary(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }



}
