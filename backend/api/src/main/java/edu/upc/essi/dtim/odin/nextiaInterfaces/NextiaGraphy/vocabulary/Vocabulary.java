package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary;

public enum Vocabulary {
    IntegrationClass(Namespaces.NEXTIADI.getElement() + "IntegratedClass"),
    IntegrationDProperty(Namespaces.NEXTIADI.getElement() + "IntegratedDatatypeProperty"),
    IntegrationOProperty(Namespaces.NEXTIADI.getElement() + "IntegratedObjectProperty"),

    JoinProperty(Namespaces.NEXTIADI.getElement() + "JoinProperty"),
    JoinObjectProperty(Namespaces.NEXTIADI.getElement() + "JoinObjectProperty");

    private String element;

    Vocabulary(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
    public void setElement(String element) {
        this.element = element;
    }


}
