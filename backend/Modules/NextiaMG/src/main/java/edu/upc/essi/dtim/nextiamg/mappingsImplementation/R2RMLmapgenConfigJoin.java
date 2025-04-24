package edu.upc.essi.dtim.nextiamg.mappingsImplementation;

import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.MappingsGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.vocabulary.R2RML;
import edu.upc.essi.dtim.nextiadi.config.Namespaces;
import edu.upc.essi.dtim.nextiadi.config.Vocabulary;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.IMapgen;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenODIN;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenResult;
import edu.upc.essi.dtim.nextiamg.utils.ConfigLoader;
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
public class R2RMLmapgenConfigJoin extends MappingType implements IMapgen<Graph>, MapgenODIN {

    private static final Logger LOGGER = Logger.getLogger(R2RMLmapgenConfigJoin.class.getName());

    // Configuration constants - could be moved to a config file or made configurable via constructor
    private static final String NEXTIADI_NS = "http://www.essi.upc.edu/DTIM/NextiaDI/";


    protected final Graph graphI;
    protected final MappingsGraph graphM;

    // Configuration properties
    private String dataNamespace;
    private Map<String, String> idColumnPerTable = new HashMap<>();
    private String tablePrefix;

    // registry to store the class triple maps
    private final Map<Resource, Resource> triplesMapRegistry = new HashMap<>();


    /**
     * Creates a new R2RMLmapgen with the provided integrated graph and default settings.
     *
     * @param graphI The integrated graph to generate mappings from
     * @param configFilePath The path to the configuration file
     */
    public R2RMLmapgenConfigJoin(Graph graphI, String configFilePath) {
        this.graphI = graphI;
        this.graphM = (MappingsGraph) CoreGraphFactory.createGraphInstance("mappings");

        // Load configuration
        ConfigLoader.loadProperties(configFilePath);
        this.dataNamespace = ConfigLoader.getProperty("DATA_NAMESPACE", "http://mydata.example.org/");
        for (String key : ConfigLoader.getAllKeys()) {
            if (key.startsWith("ID_COLUMN.")) {
                String table = key.substring("ID_COLUMN.".length());
                String idCol = ConfigLoader.getProperty(key);
                this.idColumnPerTable.put(table, idCol);
            }
        }
        this.tablePrefix = ConfigLoader.getProperty("TABLE_PREFIX", "UC3\"");

        LOGGER.info("Loaded configuration: " +
                "\nDATA_NAMESPACE=" + dataNamespace +
                "\nTABLE_PREFIX=" + tablePrefix);
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

        modelM.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

        modelM.setNsPrefix("mydata", dataNamespace);

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
                String resourceURI = objResource.getURI();
                if (resourceURI.equals(Vocabulary.IntegrationClass.val()) ||
                        resourceURI.equals(Vocabulary.IntegrationDProperty.val())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a property is a Join property (JoinProperty or JoinObjectProperty).
     *
     * @param property The resource to check
     * @return true if the resource is a join property, false otherwise
     */
    private boolean isJoinProperty(Resource property) {
        StmtIterator stmts = graphI.getGraph().listStatements(property, RDF.type, (RDFNode) null);

        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            RDFNode object = stmt.getObject();

            if (object.isResource()) {
                Resource objResource = object.asResource();
                String resourceURI = objResource.getURI();
                if (resourceURI.equals(Vocabulary.JoinProperty.val()) ||
                        resourceURI.equals(Vocabulary.JoinObjectProperty.val())) {
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
    private Resource getIntegratedClass(Resource clazz) {
        StmtIterator subclassStmt = graphI.getGraph().listStatements(clazz, RDFS.subClassOf, (RDFNode) null);

        while (subclassStmt.hasNext()) {
            Statement stmt = subclassStmt.nextStatement();
            if (stmt.getObject().isResource()) {
                Resource mainclass = stmt.getObject().asResource();
                if (isIntegratedResource(mainclass)) {
                    return mainclass;
                }
            }
        }

        return null;
    }

    /**
     * Gets the integrated property that a property is related to (via subPropertyOf).
     *
     * @param property The property to check
     * @return The integrated resource, or null if none found
     */

    private Resource getIntegratedProperty(Resource property) {
        StmtIterator subclassStmt = graphI.getGraph().listStatements(property, RDFS.subPropertyOf, (RDFNode) null);

        while (subclassStmt.hasNext()) {
            Statement stmt = subclassStmt.nextStatement();
            if (stmt.getObject().isResource()) {
                Resource mainproperty = stmt.getObject().asResource();
                if (isIntegratedResource(mainproperty)) {
                    return mainproperty;
                }
            }
        }

        return null;
    }

    /**
     * Gets the join property that a property is related to (via JoinProperty).
     *
     * @param property The property to check
     * @return The join property, or null if none found
     */
    private Resource getJoinProperty(Resource property) {
        Property joinProp = graphI.getGraph().getProperty(Vocabulary.JoinProperty.val());
        StmtIterator subclassStmt = graphI.getGraph().listStatements(property, joinProp, (RDFNode) null);

        while (subclassStmt.hasNext()) {
            Statement stmt = subclassStmt.nextStatement();
            if (stmt.getObject().isResource()) {
                Resource mainproperty = stmt.getObject().asResource();
                if (isJoinProperty(mainproperty)) {
                    return mainproperty;
                }
            }
        }

        return null;
    }

    /**
     * Gets the integrated property that a property is related to (via subPropertyOf).
     *
     * @param property The property to check
     * @return The integrated resource, or null if none found
     */
    private Resource getParentPropertyResource(Resource property) {
        Property subPropertyOf = RDFS.subPropertyOf;

        StmtIterator it = graphI.getGraph().listStatements(property, subPropertyOf, (RDFNode) null);
        if (it.hasNext()) {
            Statement stmt = it.nextStatement();
            RDFNode parent = stmt.getObject();
            if (parent.isResource()) {
                return parent.asResource();
            }
        }
        return null; // No parent found or not a resource
    }


    /**
     * Gets the TriplesMap resource of a given property via its subject class.
     *
     * @param prop The property to get the triplesMap
     * @return The triplesMap if found, or a generated one if not found
     */
    private Resource getTriplesMap(Resource prop) {

        Model modelM = graphM.getGraph();

        // Get the class (domain) of the property
        StmtIterator properties = graphI.getGraph().listStatements(prop, RDFS.domain, (RDFNode) null);

        if (!properties.hasNext()) {
            System.err.println("Warning: No rdfs:domain found for property " + prop);
            return null; // Or consider throwing an exception
        }

        Statement stmt = properties.nextStatement();
        Resource clazz = stmt.getObject().asResource();

        // Check if a TriplesMap already exists
        Resource triplesMap = triplesMapRegistry.get(clazz);
        if (triplesMap == null) {
            // Generate a URI for the TriplesMap based on the class local name
            String classLocalName = clazz.getLocalName() != null ? clazz.getLocalName() : "UnnamedClass";
            String mapURI = "http://www.essi.upc.edu/DTIM/NextiaDI/" + classLocalName + "TriplesMap";

            // Create the TriplesMap as a named resource
            triplesMap = modelM.createResource(mapURI);
            triplesMap.addProperty(RDF.type, R2RML.TriplesMap);

            // Store in the registry
            triplesMapRegistry.put(clazz, triplesMap);
        }

        return triplesMap;
    }


    /**
     * Creates a triples map for a class.
     *
     * @param clazz The class to create a triples map for
     */
    private void createTriplesMap(Resource clazz) {
        Model modelM = graphM.getGraph();

        // Look if there already exists a triples map, otherwise create one named based on the class
        Resource triplesMap = triplesMapRegistry.get(clazz);
        if (triplesMap == null) {
            // Generate a URI for the TriplesMap based on the class local name
            String classLocalName = clazz.getLocalName(); // e.g., "orders"
            String mapURI = "http://www.essi.upc.edu/DTIM/NextiaDI/" + classLocalName + "TriplesMap";

            // Create the TriplesMap as a named resource
            triplesMap = modelM.createResource(mapURI);
            triplesMap.addProperty(RDF.type, R2RML.TriplesMap);

            // Store in the registry (important for join properties)
            triplesMapRegistry.put(clazz, triplesMap);
        }


        // Create logical table
        Resource logicalTable = modelM.createResource();
        triplesMap.addProperty(R2RML.logicalTable, logicalTable);

        // TODO: Get table name from project (but it will need some kind of mapping), look where uri is generated for each attribute
        // do it with standard RDFS annotations (e.g., rdfs:label or rdfs:comment).
        String tableName = tablePrefix + getTableName(clazz);
        logicalTable.addProperty(R2RML.tableName, modelM.createLiteral(tableName));

        // Create subject map
        Resource subjectMap = modelM.createResource();
        triplesMap.addProperty(R2RML.subjectMap, subjectMap);

        // Handle class type
        Resource integratedClass = getIntegratedClass(clazz);
        if (integratedClass != null) {
            subjectMap.addProperty(R2RML.classType, integratedClass);
        }

        // Generate template for subject
        String template = generateID(integratedClass != null ? integratedClass : clazz);
        subjectMap.addProperty(R2RML.template, template);

        subjectMap.addProperty(R2RML.termType, R2RML.IRI);

        // Process properties
        processProperties(clazz, triplesMap);
    }

    /**
     * Processes a simple property and adds it to the triples map.
     *
     */
    private void processProperty(Resource property,  Resource predicateObjectMap) {
        Model modelM = graphM.getGraph();

        predicateObjectMap.addProperty(R2RML.predicate, property);

        Statement propstmt = graphI.getGraph().getProperty(property, RDFS.range);

        // Create object map
        Resource objectMap = modelM.createResource();
        predicateObjectMap.addProperty(R2RML.objectMap, objectMap);

        objectMap.addProperty(R2RML.column, getSourceReference(property));

        if (propstmt != null && propstmt.getObject().isResource()) {
            Resource datatype = propstmt.getObject().asResource();
            // Store range information for possible use in object map
            objectMap.addProperty(R2RML.datatype, datatype);
        } else {
            System.out.println("No range found for the property.");
        }
    }

    // TODO: Improve this part to connect to datasources (maybe through API?? through ConfigFile??)
    private String getSourceReference(Resource property) {
        return property.getLocalName();
    }

    /**
     * Processes an integrated property and adds it to the triples map.
     *
     */
    private void processIntegratedProperty(Resource integratedProperty, Statement stmt, Resource predicateObjectMap) {
        Model modelM = graphM.getGraph();

        // Add the predicate
        predicateObjectMap.addProperty(R2RML.predicate, integratedProperty);

        // Create the object map
        Resource objectMap = modelM.createResource();
        predicateObjectMap.addProperty(R2RML.objectMap, objectMap);

        // Determine column name: always based on the predicate (i.e., property name)
        objectMap.addProperty(R2RML.column, integratedProperty.getLocalName());

        // Add datatype from RDFS range, if available
        Statement rangeStmt = modelM.getProperty(integratedProperty, RDFS.range);
        if (rangeStmt != null && rangeStmt.getObject().isResource()) {
            objectMap.addProperty(R2RML.datatype, rangeStmt.getObject());
        }

        // Optional: also check for literal with explicit datatype
        if (stmt.getObject().isLiteral()) {
            Literal lit = stmt.getObject().asLiteral();
            if (lit.getDatatypeURI() != null) {
                objectMap.addProperty(R2RML.datatype, modelM.createResource(lit.getDatatypeURI()));
            }
        }
    }


    /**
     * Processes a join property and adds it to the triples map.
     *
     */
    private void processJoinProperty(Resource joinResource, Resource integratedPropery, Resource prop, Resource triplesMap) {
        Model modelM = graphM.getGraph();
        Model modelI = graphI.getGraph();
        StmtIterator properties = modelI.listStatements((Resource) null, RDFS.subPropertyOf, integratedPropery);

        while (properties.hasNext()) {
            Statement stmt = properties.nextStatement();
            Property joinProp = modelI.getProperty(Vocabulary.JoinProperty.val());
            Resource nprop = stmt.getSubject().asResource();
            if (!modelI.contains(nprop, joinProp, joinResource) && !prop.equals(nprop)) {
                Resource predicateObjectMapNew = modelM.createResource();
                triplesMap.addProperty(R2RML.predicateObjectMap, predicateObjectMapNew);
                predicateObjectMapNew.addProperty(R2RML.predicate, joinResource);

                // Create object map
                Resource objectMapNew = modelM.createResource();
                predicateObjectMapNew.addProperty(R2RML.objectMap, objectMapNew);

                // first get the class of the property
                objectMapNew.addProperty(R2RML.parentTriplesMap, getTriplesMap(nprop));

                // Create join condition
                Resource joinCondition = modelM.createResource();
                objectMapNew.addProperty(R2RML.joinCondition, joinCondition);
                joinCondition.addProperty(R2RML.child, getSourceReference(prop));
                joinCondition.addProperty(R2RML.parent, getSourceReference(nprop));
            }
        }
    }

    /**
     * Checks if a property is not an identifier.
     *
     * @param property The property to check
     * @return true if the property is not an identifier, false otherwise
     */
    private boolean notIdentifier(Resource property){
        String propertyName = getSourceReference(property);
        // get table name
        String tableName = getTableName(property);
        String idColumn = idColumnPerTable.get(tableName);

        return idColumn != null && !propertyName.equals(idColumn);
    }

    /**
     * Processes properties related to a class and adds them to the triples map.
     *
     * @param clazz The class to process properties for
     * @param triplesMap The triples map to add property mappings to
     */
    private void processProperties(Resource clazz, Resource triplesMap) {
        Model modelM = graphM.getGraph();
        StmtIterator properties = graphI.getGraph().listStatements((Resource) null, RDFS.domain, clazz);

        while (properties.hasNext()) {
            Statement stmt = properties.nextStatement();
            Resource res = stmt.getSubject().asResource();

            // Check if the property is a join property or just an integrated property
            if (!isIntegratedResource(res) && !isJoinProperty(res)) {
                // Create predicate-object map
                Resource predicateObjectMap = modelM.createResource();
                triplesMap.addProperty(R2RML.predicateObjectMap, predicateObjectMap);

                // set target predicate
                Resource tpredicate = res;
                Resource parentProperty = getParentPropertyResource(res);
                if (parentProperty != null) {
                    tpredicate = parentProperty;
                    Resource joinProperty = getJoinProperty(tpredicate);
                    if (joinProperty != null && isIntegratedResource(tpredicate) && notIdentifier(res)) {
                        tpredicate = joinProperty;
                        // hi ha algun problema amb els blank nodes, potser comprovar que nomes es fagi per aquelels classes que no sigui ID?
                        processJoinProperty(tpredicate, parentProperty, res, triplesMap);
                    } else {
                        processIntegratedProperty(tpredicate, stmt, predicateObjectMap);
                    }
                } else {
                    processProperty(tpredicate, predicateObjectMap);
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
        if (idColumnPerTable.isEmpty()) {
            return dataNamespace + resource.getLocalName().toLowerCase() + "/{" + resource.getLocalName().toLowerCase() + "}";
        } else {
            return dataNamespace + resource.getLocalName().toLowerCase() + "/{" + idColumnPerTable.get(resource.getLocalName()) + "}";
        }
    }

    /**
     * Sets the data namespace to use for ID templates.
     *
     * @param dataNamespace The data namespace
     * @return This instance for method chaining
     */
    public R2RMLmapgenConfigJoin withDataNamespace(String dataNamespace) {
        this.dataNamespace = dataNamespace;
        return this;
    }

    /**
     * Sets the table prefix to use for table names.
     *
     * @param tablePrefix The table prefix
     * @return This instance for method chaining
     */
    public R2RMLmapgenConfigJoin withTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        return this;
    }

    @Override
    public MapgenResult generateMappingsResult() {
        generateMappings();
        return new MapgenResult(graphM);
    }

    public static void main(String[] args) throws FileNotFoundException {

        Graph integratedGraph = new IntegratedGraphJenaImpl();
        integratedGraph.setGraph(RDFDataMgr.loadModel("/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/integrated_graph.ttl"));
        String configurationFilePath = "/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/config.properties";

        R2RMLmapgenConfigJoin r2RMLmapgen = new R2RMLmapgenConfigJoin(integratedGraph, configurationFilePath);

        MappingsGraph graphM = r2RMLmapgen.generateMappings();
        Model modelM = graphM.getGraph();

        // write the model to a TTL file
        try {
            RDFDataMgr.write((OutputStream) new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops/Cyclops-Test/generated_mappings.ttl"), modelM, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
