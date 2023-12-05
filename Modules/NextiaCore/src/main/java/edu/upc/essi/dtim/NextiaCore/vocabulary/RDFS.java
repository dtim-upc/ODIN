package edu.upc.essi.dtim.NextiaCore.vocabulary;

public class RDFS {
    public static final String uri = "http://www.w3.org/2000/01/rdf-schema#";

    /** returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }


    protected static String resource(String local)
    { return uri + local; }

    public static final String label = Init.label();
    public static final String Class = Init.Class();
    public static final String ContainerMembershipProperty = Init.containerMembershipProperty();

    public static final String domain = Init.domain();
    public static final String range = Init.range();

    public static class Init {
        public static String label() {return resource("label");}
        public static String domain() {return resource ("domain");}

        public static String range() {return resource ("range");}

        public static String Class() {return resource("Class");}
        public static String containerMembershipProperty() {return resource("ContainerMembershipProperty");}
    }

}