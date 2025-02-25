package edu.upc.essi.dtim.nextiamg.mappingsImplementation;

import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.MappingsGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.vocabulary.R2RML;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.IMapgen;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenODIN;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenResult;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * R2RMLmapgen generates R2RML mappings from an integrated graph.
 * It implements IMapgen and MapgenODIN interfaces for compatibility with the mapping system.
 */
public class R2RMLmapgen extends MappingType implements IMapgen<Graph>, MapgenODIN {

    private static final Logger LOGGER = Logger.getLogger(R2RMLmapgen.class.getName());

    // Configuration constants - could be moved to a config file or made configurable via constructor
    private static final String NEXTIADI_NS = "http://www.essi.upc.edu/DTIM/NextiaDI/";
    private static final String DEFAULT_DATA_NS = "http://mydata.example.org/";
    private static final String DEFAULT_ID_COLUMN = "expedient_number";
    private static final String DEFAULT_TABLE_PREFIX = "\"lenses\".\"lake.uc3\".\"";

    // Constants for integrated resource types
    private static final String INTEGRATED_CLASS = NEXTIADI_NS + "IntegratedClass";
    private static final String INTEGRATED_DATATYPE_PROPERTY = NEXTIADI_NS + "IntegratedDatatypeProperty";

    // Cache for commonly used resources
    private final Map<String, Resource> resourceCache = new HashMap<>();

    protected final Graph graphI;
    protected final MappingsGraph graphM;

    // Configuration properties
    private String dataNamespace;
    private String idColumnName;
    private String tablePrefix;

    /**
     * Creates a new R2RMLmapgen with the provided integrated graph and default settings.
     *
     * @param graphI The integrated graph to generate mappings from
     */
    public R2RMLmapgen(Graph graphI) {
        this(graphI, DEFAULT_DATA_NS, DEFAULT_ID_COLUMN, DEFAULT_TABLE_PREFIX);
    }

    /**
     * Creates a new R2RMLmapgen with custom configuration.
     *
     * @param graphI The integrated graph to generate mappings from
     * @param dataNamespace Namespace for generated resource URIs
     * @param idColumnName Column name to use for ID templates
     * @param tablePrefix Prefix for table names
     */
    public R2RMLmapgen(Graph graphI, String dataNamespace, String idColumnName, String tablePrefix) {
        this.graphI = graphI;
        this.graphM = (MappingsGraph) CoreGraphFactory.createGraphInstance("mappings");
        this.dataNamespace = dataNamespace;
        this.idColumnName = idColumnName;
        this.tablePrefix = tablePrefix;

        initializeCommonResources();
    }

    /**
     * Initialize and cache commonly used resources
     */
    private void initializeCommonResources() {
        resourceCache.put(INTEGRATED_CLASS, graphI.getGraph().createResource(INTEGRATED_CLASS));
        resourceCache.put(INTEGRATED_DATATYPE_PROPERTY, graphI.getGraph().createResource(INTEGRATED_DATATYPE_PROPERTY));
    }

    /**
     * Generates R2RML mappings based on the integrated graph.
     *
     * @return The generated mappings graph
     */
    public MappingsGraph generateMappings() {
        Model modelM = graphM.getGraph();

        // Add common prefixes
        modelM.setNsPrefix("rdf", RDF.getURI());
        modelM.setNsPrefix("rdfs", RDFS.getURI());
        modelM.setNsPrefix("rr", R2RML.getURI());
        modelM.setNsPrefix("ex", "http://example.org/");
        modelM.setNsPrefix("nextiaDIschema", NEXTIADI_NS);

        // Process all classes in the integrated graph
        ResIterator classes = graphI.getGraph().listSubjectsWithProperty(RDF.type, RDFS.Class);
        while (classes.hasNext()) {
            Resource clazz = classes.nextResource();
            if (!isIntegratedResource(clazz)) {
                try {
                    createTriplesMap(clazz);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error creating triples map for " + clazz.getURI(), e);
                }
            }
        }

        return this.graphM;
    }

    /**
     * Checks if a resource is an integrated resource (IntegratedClass or IntegratedDatatypeProperty).
     *
     * @param resource The resource to check
     * @return true if the resource is an integrated resource, false otherwise
     */
    private boolean isIntegratedResource(Resource resource) {
        StmtIterator stmts = graphI.getGraph().listStatements(resource, RDF.type, (RDFNode) null);

        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            RDFNode object = stmt.getObject();

            if (object.isResource()) {
                Resource objResource = object.asResource();
                if (objResource.equals(resourceCache.get(INTEGRATED_CLASS)) ||
                        objResource.equals(resourceCache.get(INTEGRATED_DATATYPE_PROPERTY))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the integrated resource that a class is related to (via subClassOf).
     *
     * @param clazz The class to check
     * @return The integrated resource, or null if none found
     */
    private Resource getIntegratedResourceRelation(Resource clazz) {
        StmtIterator subclassStmt = graphI.getGraph().listStatements(clazz, RDFS.subClassOf, (RDFNode) null);

        while (subclassStmt.hasNext()) {
            Statement stmt = subclassStmt.nextStatement();
            if (stmt.getObject().isResource()) {
                Resource subclass = stmt.getObject().asResource();
                if (isIntegratedResource(subclass)) {
                    return subclass;
                }
            }
        }

        return null;
    }

    /**
     * Creates a triples map for a class.
     *
     * @param clazz The class to create a triples map for
     */
    private void createTriplesMap(Resource clazz) {
        Model modelM = graphM.getGraph();

        // Create anonymous TriplesMap node
        Resource triplesMap = modelM.createResource();
        triplesMap.addProperty(RDF.type, R2RML.TriplesMap);

        // Create logical table
        Resource logicalTable = modelM.createResource();
        triplesMap.addProperty(R2RML.logicalTable, logicalTable);

        // Generate and add table name
        String tableName = tablePrefix + getTableName(clazz) + "\"";
        logicalTable.addProperty(R2RML.tableName, modelM.createLiteral(tableName));

        // Create subject map
        Resource subjectMap = modelM.createResource();
        triplesMap.addProperty(R2RML.subjectMap, subjectMap);

        // Handle class type
        Resource integratedClass = getIntegratedResourceRelation(clazz);
        if (integratedClass != null) {
            subjectMap.addProperty(R2RML.classType, integratedClass);
        }

        // Generate template for subject
        String template = generateID(integratedClass != null ? integratedClass : clazz);
        subjectMap.addProperty(R2RML.template, template);

        // Process properties
        processProperties(clazz, triplesMap);
    }

    /**
     * Processes properties related to a class and adds them to the triples map.
     *
     * @param clazz The class to process properties for
     * @param triplesMap The triples map to add property mappings to
     */
    private void processProperties(Resource clazz, Resource triplesMap) {
        Model modelM = graphM.getGraph();
        StmtIterator properties = graphI.getGraph().listStatements((Resource) null, null, clazz);

        while (properties.hasNext()) {
            Statement stmt = properties.nextStatement();
            Resource res = stmt.getSubject().asResource();
            Property prop = stmt.getPredicate();

            // Skip rdf:type as it's handled in subject map
            if (prop.equals(RDF.type)) continue;

            // Create predicate-object map
            Resource predicateObjectMap = modelM.createResource();
            triplesMap.addProperty(R2RML.predicateObjectMap, predicateObjectMap);

            // Process subproperty and range information
            StmtIterator subpropIterator = graphI.getGraph().listStatements(res, null, (RDFNode) null);
            while (subpropIterator.hasNext()) {
                Statement subpropStmt = subpropIterator.nextStatement();
                Property pred = subpropStmt.getPredicate();

                if (pred.equals(RDFS.subPropertyOf) && subpropStmt.getObject().isResource()) {
                    Resource subpropRes = subpropStmt.getObject().asResource();
                    predicateObjectMap.addProperty(R2RML.predicate, subpropRes);
                } else if (pred.equals(RDFS.range) && subpropStmt.getObject().isResource()) {
                    Resource datatype = subpropStmt.getObject().asResource();
                    // Store range information for possible use in object map
                    predicateObjectMap.addProperty(R2RML.datatype, datatype);
                }
            }

            // Create object map
            Resource objectMap = modelM.createResource();
            predicateObjectMap.addProperty(R2RML.objectMap, objectMap);

            if (stmt.getObject().isResource()) {
                Resource objResource = stmt.getObject().asResource();
                if (isIntegratedResource(objResource)) {
                    objectMap.addProperty(R2RML.template, generateID(objResource));
                } else {
                    objectMap.addProperty(R2RML.column, res.getLocalName());
                }
            } else if (stmt.getObject().isLiteral()) {
                objectMap.addProperty(R2RML.column, res.getLocalName());

                // Handle literal datatype
                Literal lit = stmt.getObject().asLiteral();
                if (lit.getDatatype() != null) {
                    Resource datatypeResource = modelM.createResource(lit.getDatatype().getURI());
                    objectMap.addProperty(R2RML.datatype, datatypeResource);
                }
            }
        }
    }

    /**
     * Extracts a table name from a class URI.
     *
     * @param clazz The class to get a table name for
     * @return The extracted table name
     */
    private String getTableName(Resource clazz) {
        String uri = clazz.getNameSpace();
        String[] parts = uri.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Generates an ID template for a resource.
     *
     * @param resource The resource to generate an ID template for
     * @return The generated ID template
     */
    private String generateID(Resource resource) {
        return dataNamespace + resource.getLocalName().toLowerCase() + "/{\"" + idColumnName + "\"}";
    }

    /**
     * Sets the data namespace to use for ID templates.
     *
     * @param dataNamespace The data namespace
     * @return This instance for method chaining
     */
    public R2RMLmapgen withDataNamespace(String dataNamespace) {
        this.dataNamespace = dataNamespace;
        return this;
    }

    /**
     * Sets the ID column name to use for ID templates.
     *
     * @param idColumnName The ID column name
     * @return This instance for method chaining
     */
    public R2RMLmapgen withIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
        return this;
    }

    /**
     * Sets the table prefix to use for table names.
     *
     * @param tablePrefix The table prefix
     * @return This instance for method chaining
     */
    public R2RMLmapgen withTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        return this;
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
            RDFDataMgr.write((OutputStream) new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/generated_mappingsV3.ttl"), modelM, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
