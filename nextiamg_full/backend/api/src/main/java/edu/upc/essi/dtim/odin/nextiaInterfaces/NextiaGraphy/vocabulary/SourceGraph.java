package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary;

public enum SourceGraph {

    DATA_SOURCE(Namespaces.S.getElement() + "DataSource"),
    WRAPPER(Namespaces.S.getElement() + "Wrapper"),
    ATTRIBUTE(Namespaces.S.getElement() + "Attribute"),

    HAS_WRAPPER(Namespaces.S.getElement() + "hasWrapper"),
    HAS_ATTRIBUTE(Namespaces.S.getElement() + "hasAttribute");


    private String element;

    SourceGraph(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
    public void setElement(String element) {
        this.element = element;
    }
}