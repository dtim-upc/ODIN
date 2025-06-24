package edu.upc.essi.dtim.NextiaCore.vocabulary;

public class XSD {
    public static final String uri = "http://www.w3.org/2001/XMLSchema#";
    public static String getURI() {
        return uri;
    }


    protected static String resource(String local)
    { return uri + local; }

    public static final String xstring = Init.xstring();
    public static final String xint = Init.xint();
    public static final String xdecimal = Init.xdecimal();
    public static final String xboolean = Init.xboolean();
    public static final String xdate = Init.xdate();
    public static final String xdateTime = Init.xdateTime();


    public static class Init {
        public static String xstring() {return resource("String");}
        public static String xint() {return resource("int");}

        public static String xdecimal() {return resource("decimal");}
        public static String xboolean() {return resource("boolean");}
        public static String xdate() {return resource("date");}
        public static String xdateTime() {return resource("dateTime");}
    }

}
