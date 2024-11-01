package edu.upc.essi.dtim.nextiadi.config;


public enum DataSourceVocabulary {

    DataSource(Namespaces.NextiaDI.val() +"DataSource"),

    Schema( DataSource.val() + "/Schema/" ),
    HAS_SEPARATOR(DataSource.val()+"/separator"),
    HAS_PATH(DataSource.val()+"/path"),
    HAS_FORMAT(DataSource.val()+"/format"),
    HAS_ID(DataSource.val() + "/id"),
    ALIAS(DataSource.val() + "/alias"),
    HAS_WRAPPER(DataSource.val() + "/wrapper");

    private String element;

    DataSourceVocabulary(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }

}
