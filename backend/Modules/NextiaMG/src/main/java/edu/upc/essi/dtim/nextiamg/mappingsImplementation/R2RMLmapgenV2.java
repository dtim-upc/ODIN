package edu.upc.essi.dtim.nextiamg.mappingsImplementation;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.MappingsGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.vocabulary.R2RML;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.IMapgen;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenODIN;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenResult;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.LiteralImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.function.Predicate;


public class R2RMLmapgenV2 extends MappingType implements IMapgen<Graph>, MapgenODIN {

    protected Graph graphI;
    protected MappingsGraph graphM;

    public R2RMLmapgenV2(Graph graphI) {
        this.graphI = graphI;
        this.graphM = (MappingsGraph) CoreGraphFactory.createGraphInstance("mappings");
    }

    public MappingsGraph generateMappings() {
        Model modelI = graphI.getGraph();
        Model modelM = graphM.getGraph();
        ResIterator classes = modelI.listSubjectsWithProperty(RDF.type, RDFS.Class);

        // Add common prefixes
        modelM.setNsPrefix("rdf", RDF.getURI());
        modelM.setNsPrefix("rdfs", RDFS.getURI());
        modelM.setNsPrefix("rr", R2RML.getURI());
        modelM.setNsPrefix("ex", "http://example.org/");
        modelM.setNsPrefix("nextiaDIschema", "http://www.essi.upc.edu/DTIM/NextiaDI/");

        while (classes.hasNext()) {
            Resource clazz = classes.nextResource();
            if (!isIntegratedResource(clazz)) {
                createTriplesMap(clazz);
            }
        }
        return this.graphM;
    }

    // To do: look for <http://www.essi.upc.edu/DTIM/NextiaDI/IT_OBJECT_1>
    //        a       <http://www.essi.upc.edu/DTIM/NextiaDI/IntegratedClass> ;
    // or
    // <http://www.essi.upc.edu/DTIM/NextiaDI/IT_URGENCY_NAME>
    //        a       <http://www.essi.upc.edu/DTIM/NextiaDI/IntegratedDatatypeProperty> ;
    private boolean isIntegratedResource(Resource clazz) {
        Property rdfType = RDF.type;
        Resource integratedClass = graphI.getGraph().createResource("http://www.essi.upc.edu/DTIM/NextiaDI/IntegratedClass");
        Resource integratedDatatypeProperty = graphI.getGraph().createResource("http://www.essi.upc.edu/DTIM/NextiaDI/IntegratedDatatypeProperty");

        // Check if clazz is of type IntegratedClass or IntegratedDatatypeProperty
        StmtIterator integratedStmt = graphI.getGraph().listStatements(clazz, rdfType, (RDFNode) null);

        while (integratedStmt.hasNext()) {
            Statement stmt = integratedStmt.nextStatement();
            RDFNode object = stmt.getObject();

            if (object.isResource()) {
                Resource objResource = object.asResource();
                if (objResource.equals(integratedClass) || objResource.equals(integratedDatatypeProperty)) {
                    return true;
                }
            }
        }

        return false;
    }


    private Resource getIntegratedResourceSubclass(Resource clazz) {
        StmtIterator subclassStmt = graphI.getGraph().listStatements(null, RDFS.subClassOf, clazz);
        while (subclassStmt.hasNext()) {
            Statement stmt = subclassStmt.nextStatement();
            Resource subclass = stmt.getSubject();

            if (isIntegratedResource(subclass)) {
                return subclass;
            }
        }
        return null;
    }

    private Resource getIntegratedResource(Resource clazz) {
        StmtIterator subclassStmt = graphI.getGraph().listStatements(clazz, RDFS.subClassOf, (RDFNode) null);
        while (subclassStmt.hasNext()) {
            Statement stmt = subclassStmt.nextStatement();
            Resource subclass = (Resource) stmt.getObject();

            if (isIntegratedResource(subclass)) {
                return subclass;
            }
        }
        return null;
    }

    private void createTriplesMap(Resource clazz) {
        // Create anonymous TriplesMap node
        Resource triplesMap = graphM.getGraph().createResource();
        triplesMap.addProperty(RDF.type, R2RML.TriplesMap);

        // Create logical table
        Resource logicalTable = graphM.getGraph().createResource();
        triplesMap.addProperty(R2RML.logicalTable, logicalTable);

        // Add table name with proper escaping
        String tableName = "\"lenses\".\"lake.uc3\".\"" + getTableName(clazz) + "\"";
        logicalTable.addProperty(R2RML.tableName, graphM.getGraph().createLiteral(tableName));

        // Create subject map
        Resource subjectMap = graphM.getGraph().createResource();
        triplesMap.addProperty(R2RML.subjectMap, subjectMap);

        // Handle class type
//        StmtIterator typeStmts = graphI.getGraph().listStatements(clazz, RDF.type, (RDFNode) null);
//        while (typeStmts.hasNext()) {
//            Statement typeStmt = typeStmts.nextStatement();
//            if (typeStmt.getObject().isResource()) {
//                subjectMap.addProperty(R2RML.classType, typeStmt.getObject());
//            }
//        }

        Resource integratedClass = getIntegratedResource(clazz);
        if (integratedClass != null){
            subjectMap.addProperty(R2RML.classType, integratedClass);
        }

        // Generate template based on integrated resource
        Resource integratedResourceSubclass = getIntegratedResourceSubclass(clazz);
        String template;
        if (integratedResourceSubclass != null) {
            template = generateIDWithSubclass(clazz, integratedResourceSubclass);
        } else {
            template = generateID(clazz);
        }

        subjectMap.addProperty(R2RML.template, template);

        // Process properties
        processProperties(clazz, triplesMap);
    }

    private void processProperties(Resource clazz, Resource triplesMap) {
        StmtIterator properties = graphI.getGraph().listStatements((Resource) null, null, clazz);

        while (properties.hasNext()) {
            Statement stmt = properties.nextStatement();
            Resource res = stmt.getSubject().asResource();
            Property prop = stmt.getPredicate();

            // Skip rdf:type as it's handled in subject map
            if (prop.equals(RDF.type)) continue;

            // Create predicate-object map
            Resource predicateObjectMap = graphM.getGraph().createResource();
            triplesMap.addProperty(R2RML.predicateObjectMap, predicateObjectMap);

//            if (prop.equals(RDFS.subPropertyOf)){
//                Resource subpropres = (Resource) stmt.getObject();
//                predicateObjectMap.addProperty(R2RML.predicate, subpropres);
//            }

            //Add predicate
            StmtIterator subpropiterator = graphI.getGraph().listStatements(res, null, (RDFNode) null);
            while (subpropiterator.hasNext()){
                Statement subpropstmt = subpropiterator.nextStatement();
                Property pred = subpropstmt.getPredicate();
                if (pred.equals(RDFS.subPropertyOf)){
                    Resource subpropres = (Resource) subpropstmt.getObject();
                    predicateObjectMap.addProperty(R2RML.predicate, subpropres);
                } else if (pred.equals(RDFS.range)) {
                    Resource datatype = (Resource) subpropstmt.getObject();
                    predicateObjectMap.addProperty(R2RML.datatype, datatype);
                }
            }
            // Create object map
            Resource objectMap = graphM.getGraph().createResource();
            predicateObjectMap.addProperty(R2RML.objectMap, objectMap);

            if (stmt.getObject().isResource()) {
                Resource objResource = stmt.getObject().asResource();
                if (isIntegratedResource(objResource)) {
                    objectMap.addProperty(R2RML.template, generateID(objResource));
                } else {
                    objectMap.addProperty(R2RML.column, res.getLocalName());
                }
            } else {
                objectMap.addProperty(R2RML.column, res.getLocalName());

                // Handle literal datatype using XSD vocabulary instead
                // Handle literal datatype
                Literal lit = stmt.getObject().asLiteral();
                if (lit.getDatatype() != null) {
                    // Create a Resource from the datatype URI
                    Resource datatypeResource = graphM.getGraph().createResource(lit.getDatatype().getURI());
                    Property datatypeProp = ResourceFactory.createProperty(R2RML.getURI() + "datatype");
                    objectMap.addProperty(datatypeProp, datatypeResource);
                }
            }
        }
    }

    private String getTableName(Resource clazz) {
        String uri = clazz.getNameSpace();
        // take only the last word of the URI
        String[] parts = uri.split("/");
        return parts[parts.length - 1];
    }

    private String generateID(Resource clazz) {
        return "http://mydata.example.org/" +
                clazz.getLocalName().toLowerCase() +
                "/{\"expedient_number\"}";
    }

    private String generateIDWithSubclass(Resource clazz, Resource integratedResourceSubclass) {
        return generateID(clazz) + "/" + integratedResourceSubclass.getLocalName();
    }

    @Override
    public MapgenResult generateMappings(String mappingsType, Graph graphI) {
        generateMappings();
        return new MapgenResult(graphM);
    }
    public static void main(String[] args) throws FileNotFoundException {

        Graph integratedGraph = new IntegratedGraphJenaImpl();
        integratedGraph.setGraph(RDFDataMgr.loadModel("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/integrated_mitender.ttl"));

        R2RMLmapgenV2 r2RMLmapgen = new R2RMLmapgenV2(integratedGraph);

        MappingsGraph graphM = r2RMLmapgen.generateMappings();
        Model modelM = graphM.getGraph();

        // write the model to a TTL file
        try {
            RDFDataMgr.write((OutputStream) new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/generated_mappingsV2.ttl"), modelM, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
