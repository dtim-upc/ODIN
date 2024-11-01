package edu.upc.essi.dtim.nextiadi.bootstraping.metamodels;

import org.apache.jena.graph.* ;
import org.apache.jena.rdf.model.* ;

public class JSON_MM {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri="https://www.essi.upc.edu/dtim/json-metamodel#";

    protected static final Resource resource( String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource Document   = Init.Document();
    public static final Resource Object     = Init.Object();
    public static final Resource Key        = Init.Key();
    public static final Resource DataType   = Init.DataType();
    public static final Resource Primitive  = Init.Primitive();
    public static final Resource Array      = Init.Array();
    public static final Resource Number     = Init.Number();
    public static final Resource String     = Init.String();


    public static final Property hasValue   = Init.hasValue();
    public static final Property hasKey     = Init.hasKey();
    public static final Property hasMember  = Init.hasMember();

    public static class Init {
        public static Resource Document()          { return resource( "Document"); }
        public static Resource Object()       { return resource( "Object"); }
        public static Resource Key ()     { return resource( "Key"); }
        public static Resource DataType() { return resource( "DataType");   }
        public static Resource Primitive()        { return resource( "Primitive"); }
        public static Resource Array()       { return resource( "Array"); }
        public static Resource Number()        { return resource( "Number"); }
        public static Resource String()        { return resource( "String"); }

        public static Property hasValue()        { return property( "hasValue"); }
        public static Property hasKey()         { return property( "hasKey"); }
        public static Property hasMember()          { return property( "hasMember"); }
    }

    /**
     The vocabulary, expressed for the SPI layer in terms of .graph Nodes.
     */
    @SuppressWarnings("hiding") public static class Nodes
    {
        public static final Node Document   = Init.Document().asNode();
        public static final Node Object     = Init.Object().asNode();
        public static final Node Key        = Init.Key().asNode();
        public static final Node DataType   = Init.DataType().asNode();
        public static final Node Primitive  = Init.Primitive().asNode();
        public static final Node Array      = Init.Array().asNode();
        public static final Node Number      = Init.Number().asNode();
        public static final Node String      = Init.String().asNode();

        public static final Node hasValue   = Init.hasValue().asNode();
        public static final Node hasKey     = Init.hasKey().asNode();
        public static final Node hasMember  = Init.hasMember().asNode();
    }

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }


}
