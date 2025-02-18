package edu.upc.essi.dtim.nextiamg.mappingsImplementation;

import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.vocabulary.R2RML;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.IMapgen;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenODIN;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenResult;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.*;



public class R2RMLmapgen extends MappingType implements IMapgen<Graph>, MapgenODIN {

    protected Graph graphI;
    protected MappingsGraph graphM;

    public R2RMLmapgen(Graph graphI) {
        this.graphI = graphI;
        this.graphM = (MappingsGraph) CoreGraphFactory.createGraphInstance("mappings");
    }

    public MappingsGraph generateMappings() {
        ResIterator classes = graphI.getGraph().listSubjectsWithProperty(RDF.type, RDFS.Class);

        while (classes.hasNext()) {
            Resource clazz = classes.nextResource();
            if (!isIntegratedResource(clazz)) {
                createTriplesMap(clazz);
            }
        }
        return this.graphM;
    }

    private boolean isIntegratedResource(Resource clazz) {
        return clazz.getURI().contains("IntegratedResource");
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

    private void createTriplesMap(Resource clazz) {
        // Create TriplesMap resource
        String triplesMapURI = clazz.getURI() + "_TriplesMap";
        graphM.addTriple(triplesMapURI, RDF.type.getURI(), R2RML.Init.TriplesMap().getURI());

        // Logical Table definition
        String tableName = getTableName(clazz);
        graphM.addTriple(triplesMapURI, R2RML.Init.tableName().getURI(), tableName);

        // Subject Map
        String subjectMapURI = clazz.getURI() + "_SubjectMap";

        Resource integratedResourceSubclass = getIntegratedResourceSubclass(clazz);
        if (integratedResourceSubclass != null) {
            String uriTemplate = generateIDWithSubclass(clazz, integratedResourceSubclass);
            graphM.addTriple(subjectMapURI, R2RML.Init.template().getURI(), uriTemplate);
        } else {
            graphM.addTriple(subjectMapURI, R2RML.Init.template().getURI(), generateID(clazz));
        }

        graphM.addTriple(triplesMapURI, R2RML.Init.subjectMap().getURI(), subjectMapURI);

        // Process properties (predicate-object maps)
        processProperties(clazz, triplesMapURI);
    }

    private void processProperties(Resource clazz, String triplesMapUri) {
        StmtIterator properties = graphI.getGraph().listStatements(clazz, null, (RDFNode) null);
        while (properties.hasNext()) {
            Statement stmt = properties.nextStatement();
            Property prop = stmt.getPredicate();

            Resource predicateObjectMap = graphM.getGraph().createResource();

            graphM.addTriple(triplesMapUri, R2RML.Init.predicateObjectMap().getURI(), predicateObjectMap.getURI());

            // Object Map
            if (stmt.getObject().isResource()) {
                Resource objectMap = graphM.getGraph().createResource();
                predicateObjectMap.addProperty(R2RML.Init.objectMap(), objectMap);
                objectMap.addProperty(R2RML.Init.column(), prop.getLocalName());
            } else {
                Literal objectLiteral = stmt.getObject().asLiteral();
                predicateObjectMap.addProperty(R2RML.Init.objectMap(), objectLiteral);
            }
        }
    }

    private Resource createObjectMap(Property prop, Model r2rmlModel) {
        return r2rmlModel.createResource()
                .addProperty(R2RML.Init.column(), r2rmlModel.createLiteral(prop.getLocalName()));
    }

    private String getTableName(Resource clazz) {
        return clazz.getLocalName();
    }

    private String generateID(Resource clazz) {
        return clazz.getURI() + "/{id}";
    }

    private String generateIDWithSubclass(Resource clazz, Resource integratedResourceSubclass) {
        return clazz.getURI() + "/{id}/" + integratedResourceSubclass.getLocalName();
    }

    @Override
    public MapgenResult generateMappings(String mappingsType, Graph graphI) {
        generateMappings();
        return new MapgenResult(graphM);
    }

    public static void main(String[] args) throws FileNotFoundException {

        Graph integratedGraph = new IntegratedGraphJenaImpl();
        integratedGraph.setGraph(RDFDataMgr.loadModel("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/integrated_mitender.ttl"));

        R2RMLmapgen r2RMLmapgen = new R2RMLmapgen(integratedGraph);

        MappingsGraph graphM = r2RMLmapgen.generateMappings();
        Model modelM = graphM.getGraph();

        // write the model to a TTL file
        try {
            RDFDataMgr.write((OutputStream) new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/generated_mappings.ttl"), modelM, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
