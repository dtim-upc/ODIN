package edu.upc.essi.dtim.NextiaCore.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class R2RML {
    public static final String uri = "http://www.w3.org/ns/r2rml#";

    /** Returns the URI for this schema
     * @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }

    protected static String resource(String local) {
        return uri + local;
    }

    protected static Property property(String local) {
        return ResourceFactory.createProperty(resource(local));
    }

    protected static Resource res(String local) {
        return ResourceFactory.createResource(resource(local));
    }

    public static final Resource TriplesMap = Init.TriplesMap();

    public static final Property parentTriplesMap = Init.parentTriplesMap();

    public static final Property datatype = Init.datatype();

    public static final Property classType = Init.classType();
    public static final Property termType = Init.termType();
    public static final Property logicalTable = Init.logicalTable();
    public static final Property tableName = Init.tableName();
    public static final Property subjectMap = Init.subjectMap();
    public static final Property predicateObjectMap = Init.predicateObjectMap();

    public static final Property template = Init.template();
    public static final Property predicate = Init.predicate();
    public static final Property objectMap = Init.objectMap();
    public static final Property column = Init.column();

    public static final Property joinCondition = Init.joinCondition();
    public static final Property child = Init.child();
    public static final Property parent = Init.parent();

    public static final Property IRI = Init.Iri();

    public static class Init {
        public static Resource TriplesMap() { return res("TriplesMap"); }

        public static Property parentTriplesMap() { return property("parentTriplesMap"); }

        public static Property classType() { return property("class"); }

        public static Property termType() { return property("termType"); }
        public static Property datatype() { return property("datatype"); }
        public static Property logicalTable() { return property("logicalTable"); }
        public static Property tableName() { return property("tableName"); }
        public static Property subjectMap() { return property("subjectMap"); }
        public static Property predicateObjectMap() { return property("predicateObjectMap"); }
        public static Property template() { return property("template"); }
        public static Property predicate() { return property("predicate"); }
        public static Property objectMap() { return property("objectMap"); }
        public static Property column() { return property("column"); }

        public static Property joinCondition() { return property("joinCondition"); }
        public static Property child() { return property("child"); }
        public static Property parent() { return property("parent"); }
        public static Property Iri() { return property("IRI"); }
    }
}
