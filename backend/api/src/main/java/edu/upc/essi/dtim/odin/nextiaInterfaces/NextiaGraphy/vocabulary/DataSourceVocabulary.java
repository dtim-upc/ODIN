package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary;

public enum DataSourceVocabulary {

    DataSource(Namespaces.NEXTIADI.getElement() + "DataSource"),

    Schema(DataSource.getElement() + "/Schema/"),
    HAS_SEPARATOR(DataSource.getElement() + "/separator"),
    HAS_PATH(DataSource.getElement() + "/path"),
    HAS_FORMAT(DataSource.getElement() + "/format"),
    HAS_ID(DataSource.getElement() + "/id"),
    ALIAS(DataSource.getElement() + "/alias"),
    HAS_WRAPPER(DataSource.getElement() + "/wrapper");

    private String element;

    DataSourceVocabulary(String element) {
        this.element = element;
    }

    public String getElement() { return element; }
    public void setElement(String element) {
        this.element = element;
    }

}
