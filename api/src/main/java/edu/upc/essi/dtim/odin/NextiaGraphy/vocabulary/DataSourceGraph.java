package edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary;

public enum DataSourceGraph {

    // TODO: must extend DataSourceVocabulary
    SCHEMA(Namespaces.DATASOURCE.val()+"/Schema"),

    HAS_SEPARATOR(Namespaces.DATASOURCE.val()+"/hasSeparator"),
    HAS_PATH(Namespaces.DATASOURCE.val()+"/path"),
    HAS_DESCRIPTION(Namespaces.DATASOURCE.val()+"/description"),
    HAS_FILENAME(Namespaces.DATASOURCE.val()+"/hasFileName"),
    HAS_FILESIZE(Namespaces.DATASOURCE.val()+"/hasFileSize"),
    HAS_FORMAT(Namespaces.DATASOURCE.val()+"/hasFormat"),
    HAS_ID(Namespaces.DATASOURCE.val()+"/hasID"),

    HAS_PROJECTID(Namespaces.DATASOURCE.val()+"/hasProjectID"),

    UNUSED_ALIGNMENTS(Namespaces.NEXTIADI.val() + "unusedAlignments"),
    INTEGRATION_OF(Namespaces.NEXTIADI.val() +"integrationOf"  ),
    IS_MINIMAL_OF(Namespaces.NEXTIADI.val() +"isMinimalOf"  ),
    GRAPHICAL(DataSourceVocabulary.DataSource.val() +"/graphicalGraph"),
    MINIMAL(Namespaces.NEXTIADI.val() +"minimal"  );


    private String element;

    DataSourceGraph(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }


}
