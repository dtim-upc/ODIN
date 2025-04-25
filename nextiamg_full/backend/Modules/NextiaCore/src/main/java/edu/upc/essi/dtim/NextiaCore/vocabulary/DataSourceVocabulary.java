package edu.upc.essi.dtim.NextiaCore.vocabulary;

public enum DataSourceVocabulary {

    DataSource(Namespaces.NextiaDI.getURI() +"DataSource"),

    HAS_FORMAT(DataSource.getURI()+"/format"),
    HAS_ID(DataSource.getURI() + "/id"),
    HAS_WRAPPER(DataSource.getURI() + "/wrapper"),
    HAS_PATH(DataSource.getURI()+"/path"),

    DESCRIPTION(DataSource.getURI()+"/description"),
    HAS_FILE_SIZE(DataSource.getURI()+"/hasFileSize"),
    HAS_FILE_NAME(DataSource.getURI()+"/hasFileName"),

    Schema( DataSource.getURI() + "/Schema/" ),

    HAS_SEPARATOR(DataSource.getURI()+"/separator"),
    ALIAS(DataSource.getURI() + "/alias");


    private final String element;

    DataSourceVocabulary(String element) {
        this.element = element;
    }

    public String getURI() {
        return element;
    }

}

