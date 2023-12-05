package edu.upc.essi.dtim.NextiaCore.vocabulary;

public class XSD {
    public static final String uri = "http://www.w3.org/2001/XMLSchema#";

    /** returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }


    protected static String resource(String local)
    { return uri + local; }

    public static final String xstring = Init.xstring();
    public static final String xint = Init.xint();

    public static class Init {
        public static String xstring() {return resource("String");}
        public static String xint() {return resource("int");}
    }

}
