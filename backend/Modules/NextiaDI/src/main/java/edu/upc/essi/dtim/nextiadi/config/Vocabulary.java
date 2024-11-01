package edu.upc.essi.dtim.nextiadi.config;


public enum Vocabulary {


    IntegrationClass(Namespaces.NextiaDI.val() + "IntegratedClass"),
    IntegrationDProperty( Namespaces.NextiaDI.val() + "IntegratedDatatypeProperty"),
    IntegrationOProperty( Namespaces.NextiaDI.val() + "IntegratedObjectProperty"),

    JoinProperty(Namespaces.NextiaDI.val() + "JoinProperty"),
    JoinObjectProperty(Namespaces.NextiaDI.val() + "JoinObjectProperty");

    private String element;

    Vocabulary(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }



}
