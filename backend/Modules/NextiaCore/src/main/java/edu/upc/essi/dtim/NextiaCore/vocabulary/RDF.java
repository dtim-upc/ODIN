package edu.upc.essi.dtim.NextiaCore.vocabulary;

public class RDF {
    public static final String uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /** returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }

    protected static String resource(String local)
    { return uri + local; }

    public static final String type = Init.type();

    public static final String Property = Init.Property();


    public static class Init {
        public static String type() {return resource("type");}
        public static String Property() {return resource ("Property");}
    }

}
