package edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.vocabulary;

public enum DataSourceGraph {

    // TODO: must extend DataSourceVocabulary
    SCHEMA(Namespaces.DATASOURCE.getElement() + "/Schema"),

    HAS_SEPARATOR(Namespaces.DATASOURCE.getElement() + "/hasSeparator"),
    HAS_PATH(Namespaces.DATASOURCE.getElement() + "/path"),
    HAS_DESCRIPTION(Namespaces.DATASOURCE.getElement() + "/description"),
    HAS_FILENAME(Namespaces.DATASOURCE.getElement() + "/hasFileName"),
    HAS_FILESIZE(Namespaces.DATASOURCE.getElement() + "/hasFileSize"),
    HAS_FORMAT(Namespaces.DATASOURCE.getElement() + "/hasFormat"),
    HAS_ID(Namespaces.DATASOURCE.getElement() + "/hasID"),

    HAS_PROJECTID(Namespaces.DATASOURCE.getElement() + "/hasProjectID"),

    UNUSED_ALIGNMENTS(Namespaces.NEXTIADI.getElement() + "unusedAlignments"),
    INTEGRATION_OF(Namespaces.NEXTIADI.getElement() + "integrationOf"),
    IS_MINIMAL_OF(Namespaces.NEXTIADI.getElement() + "isMinimalOf"),
    GRAPHICAL(DataSourceVocabulary.DataSource.getElement() + "/graphicalGraph"),
    MINIMAL(Namespaces.NEXTIADI.getElement() + "minimal");


    private String element;

    DataSourceGraph(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
    public void setElement(String element) {
        this.element = element;
    }


}
