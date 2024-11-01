package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary;

public enum GlobalGraph {

    FEATURE(Namespaces.G.getElement() + "Feature"),
    HAS_FEATURE(Namespaces.G.getElement() + "hasFeature");

    private String element;

    GlobalGraph(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
    public void setElement(String element) { this.element = element; }

}
