package edu.upc.essi.dtim.nextiamg.utils;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class R2RML {
    public static final String NS = "http://www.w3.org/ns/r2rml#";

    public static String getURI() {
        return NS;
    }

    public static final Resource TriplesMap = ResourceFactory.createResource(NS + "TriplesMap");


    public static final Property classType = ResourceFactory.createProperty(NS, "class");

    public static final Property logicalTable = ResourceFactory.createProperty(NS, "logicalTable");
    public static final Property tableName = ResourceFactory.createProperty(NS, "tableName");
    public static final Property subjectMap = ResourceFactory.createProperty(NS, "subjectMap");
    public static final Property predicateObjectMap = ResourceFactory.createProperty(NS, "predicateObjectMap");
    public static final Property template = ResourceFactory.createProperty(NS, "template");
    public static final Property predicate = ResourceFactory.createProperty(NS, "predicate");
    public static final Property objectMap = ResourceFactory.createProperty(NS, "objectMap");
    public static final Property column = ResourceFactory.createProperty(NS, "column");
}
