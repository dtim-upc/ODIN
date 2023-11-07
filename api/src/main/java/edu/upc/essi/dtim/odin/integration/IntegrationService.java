package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.bootstrapping.SourceService;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationData;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD.jdModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD.jdModuleInterface;
import edu.upc.essi.dtim.odin.project.Project;
import edu.upc.essi.dtim.odin.project.ProjectService;
import edu.upc.essi.dtim.odin.repositories.RepositoryService;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for integrating and manipulating RDF graphs and alignments.
 */
@Service
public class IntegrationService {
    /**
     * The dependency on the ProjectService class.
     */
    private final ProjectService projectService;
    private final AppConfig appConfig;

    /**
     * Constructor for the IntegrationService class.
     *
     * @param appConfig The application configuration.
     */
    public IntegrationService(@Autowired AppConfig appConfig, @Autowired ProjectService projectService){
        this.appConfig = appConfig;
        this.projectService = projectService;
    }

    /**
     * Integrates data from a second RDF graph into the provided integrated RDF graph based on specified alignments.
     *
     * @param integratedGraph The integrated RDF graph to which data will be integrated.
     * @param dsB             The dataset containing the second RDF graph.
     * @param alignments      A list of alignments specifying how the data should be integrated.
     * @return The integrated RDF graph with the integrated data.
     * @throws RuntimeException If there is an error while performing the integration.
     */
    public Graph integrateData(GraphJenaImpl integratedGraph, Dataset dsB, List<Alignment> alignments) {
        // Retrieve the name of the second RDF graph.
        String graphNameB = dsB.getLocalGraph().getGraphName();

        // Create an interface to interact with the graph store.
        GraphStoreInterface graphStoreInterface;
        try {
            graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            // Throw a runtime exception if there is an error while obtaining the graph store.
            throw new RuntimeException(e);
        }

        // Retrieve the local graph corresponding to graphNameB from the graph store.
        Graph localGraph = graphStoreInterface.getGraph(graphNameB);

        // Set the local graph of dsB to the retrieved local graph.
        dsB.setLocalGraph((LocalGraphJenaImpl) localGraph);

        // Get the local graph of dsB.
        Graph graphB = dsB.getLocalGraph();

        // Create an instance of the integration module.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();

        // Integrate the data from graphB into the integratedGraph based on alignments.
        Graph newIntegratedGraph = integrationInterface.integrate(integratedGraph, graphB, alignments);

        // Generate a visual schema for the new integrated graph and assign it.
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        String newGraphicalSchema = visualLibInterface.generateVisualGraph(newIntegratedGraph);
        newIntegratedGraph.setGraphicalSchema(newGraphicalSchema);

        return newIntegratedGraph;
    }

    /**
     * Retrieves a project by its unique identifier.
     *
     * @param projectId The unique identifier of the project to retrieve.
     * @return The project associated with the given identifier, or null if not found.
     */
    public Project getProject(String projectId) {
        // Create an instance of the ProjectService using the provided AppConfig.
        ProjectService projectService = new ProjectService(appConfig);

        // Retrieve the project using its unique identifier.
        return projectService.getProjectById(projectId);
    }

    /**
     * Saves or updates a project in the system.
     *
     * @param project The project to be saved or updated.
     * @return The saved or updated project with any modifications or new identifiers.
     */
    public Project saveProject(Project project) {
        // Create an instance of the ProjectService using the provided AppConfig.
        ProjectService projectService = new ProjectService(appConfig);

        // Save or update the project in the system and return the result.
        return projectService.saveProject(project);
    }

    /**
     * Generates a list of JoinAlignments based on the provided graphs and integration data.
     *
     * @param graphA   The first graph for alignment comparison.
     * @param graphB   The second graph for alignment comparison.
     * @param iData    The integration data containing alignments.
     * @return A list of JoinAlignment objects representing potential join alignments.
     */
    public List<JoinAlignment> generateJoinAlignments(Graph graphA, Graph graphB, IntegrationData iData) {
        // Create an instance of the integration module using the implementation.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();

        // Get a list of unused alignments between graphA and graphB.
        List<Alignment> unusedAlignments = integrationInterface.getUnused(graphA, graphB, iData.getAlignments());

        // Filter alignments of type "datatype" and collect their concatenated IRI values.
        Set<String> alignmentsAB = iData.getAlignments().stream()
                .filter(a -> a.getType().equals("datatype"))
                .map(al -> al.getIriA() + al.getIriB())
                .collect(Collectors.toSet());

        // Find potential join alignments by matching concatenated IRI values with unused alignments.
        List<Alignment> potentialJoinAlignments =
                unusedAlignments.stream()
                        .filter(e -> alignmentsAB.contains(e.getIriA() + e.getIriB()))
                        .collect(Collectors.toList());

        // Create a list to store JoinAlignment objects.
        List<JoinAlignment> joinProperties = new ArrayList<>();
        for (Alignment a : potentialJoinAlignments) {
            // Create a new JoinAlignment object and wrap it with the alignment data.
            JoinAlignment j = new JoinAlignment();
            j.wrap(a);

            // Get the domain information for the properties from both graphs.
            String domainA = getDomainOfProperty(graphA, a.getIriA());
            String domainB = getDomainOfProperty(graphB, a.getIriB());

            // Get the RDFS label information for the domains from both graphs.
            String domainLA = getRDFSLabel(graphA, domainA);
            String domainLB = getRDFSLabel(graphB, domainB);

            // Set the domain and domain label information in the JoinAlignment object.
            j.setDomainA(domainA);
            j.setDomainB(domainB);
            j.setDomainLabelA(domainLA);
            j.setDomainLabelB(domainLB);

            // Add the JoinAlignment object to the list.
            joinProperties.add(j);
        }
        return joinProperties;
    }

    /**
     * Retrieves the domain of a property IRI from the specified graph.
     *
     * @param graph        The graph to query for the property's domain.
     * @param propertyIRI  The IRI (Internationalized Resource Identifier) of the property.
     * @return The domain of the property if found, or null if not found.
     */
    private String getDomainOfProperty(Graph graph, String propertyIRI) {
        // Define a SPARQL query to retrieve the domain of the property.
        String query = "SELECT ?domain WHERE { <" + propertyIRI + "> <" + RDFS.domain.toString() + "> ?domain. }";

        // Execute the query on the graph.
        List<Map<String, Object>> res = graph.query(query);

        // Check if the query result is not empty.
        if (!res.isEmpty()) {
            // Extract and return the domain as a string from the query result.
            return res.get(0).get("domain").toString();
        }

        // Return null if the domain information was not found.
        return null;
    }

    /**
     * Retrieves the RDFS label of a resource IRI from the specified graph.
     *
     * @param graph        The graph to query for the resource's RDFS label.
     * @param resourceIRI  The IRI (Internationalized Resource Identifier) of the resource.
     * @return The RDFS label of the resource if found, or null if not found.
     */
    private String getRDFSLabel(Graph graph, String resourceIRI) {
        // Define a SPARQL query to retrieve the RDFS label of the resource.
        String query = "SELECT ?label WHERE { <" + resourceIRI + "> <" + RDFS.label.toString() + "> ?label. }";

        // Execute the query on the graph.
        List<Map<String, Object>> res = graph.query(query);

        // Check if the query result is not empty.
        if (!res.isEmpty()) {
            // Extract and return the RDFS label as a string from the query result.
            return res.get(0).get("label").toString();
        }

        // Return null if the RDFS label information was not found.
        return null;
    }

    /**
     * Updates the integrated graph within a project with a new integrated graph.
     *
     * @param project          The project whose integrated graph is being updated.
     * @param integratedGraph  The new integrated graph to be set in the project.
     * @return The updated project with the new integrated graph.
     */
    public Project updateIntegratedGraphProject(Project project, Graph integratedGraph) {
        // Create an instance of an integrated graph from the CoreGraphFactory.
        Graph integratedImpl = CoreGraphFactory.createIntegratedGraph();

        // Set the graph data of the integrated graph to the data from the provided integratedGraph.
        integratedImpl.setGraph(integratedGraph.getGraph());

        // Set the integrated graph in the project.
        project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedImpl);

        // Set the graphical schema of the integrated graph in the project.
        project.getIntegratedGraph().setGraphicalSchema(integratedGraph.getGraphicalSchema());

        // Return the updated project with the new integrated graph.
        return project;
    }

    /**
     * Updates the global graph within an integrated project with a new global graph.
     *
     * @param project      The project containing the integrated graph and the global graph to be updated.
     * @param globalGraph  The new global graph to be set within the project's integrated graph.
     * @return The updated project with the new global graph.
     */
    public Project updateGlobalGraphProject(Project project, Graph globalGraph) {
        // Create an instance of a global graph from the CoreGraphFactory.
        Graph globalImpl = CoreGraphFactory.createGlobalGraph();

        // Set the graph data of the global graph to the data from the provided globalGraph.
        globalImpl.setGraph(globalGraph.getGraph());

        // Set the global graph within the project's integrated graph.
        project.getIntegratedGraph().setGlobalGraph((GlobalGraphJenaImpl) globalImpl);

        // Set the graphical schema of the global graph within the project.
        project.getIntegratedGraph().getGlobalGraph().setGraphicalSchema(globalGraph.getGraphicalSchema());

        // Return the updated project with the new global graph.
        return project;
    }

    /**
     * Generates a global graph by integrating an integrated graph (integratedGraph), a dataset (dsB), and a list of alignments.
     *
     * @param integratedGraph The integrated graph to which the global graph will be added.
     * @param dsB             The dataset representing the second graph to be integrated.
     * @param alignments      The list of alignments specifying how the graphs should be integrated.
     * @return The generated global graph resulting from the integration.
     * @throws RuntimeException If an error occurs during graph integration or when accessing the graph store.
     */
    public Graph generateGlobalGraph(GraphJenaImpl integratedGraph, Dataset dsB, List<Alignment> alignments) {
        // Obtain the name of the second graph from the dataset.
        String graphNameB = dsB.getLocalGraph().getGraphName();

        GraphStoreInterface graphStoreInterface;
        try {
            // Get an instance of the graph store using the provided appConfig.
            graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            // Throw a runtime exception if an error occurs while getting the graph store instance.
            throw new RuntimeException(e);
        }

        // Get the local graph corresponding to graphNameB from the graph store.
        Graph localGraph = graphStoreInterface.getGraph(graphNameB);

        // Set the local graph of dataset dsB to the obtained localGraph.
        dsB.setLocalGraph((LocalGraphJenaImpl) localGraph);

        // Retrieve the local graph of dataset dsB.
        Graph graphB = dsB.getLocalGraph();

        // Create an instance of the integration module.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();

        // Integrate the integrated graph, graphB, and alignments to generate the global graph.
        Graph globalGraph = integrationInterface.globalGraph(integratedGraph, graphB, alignments);

        // Write the global graph to a file or location (e.g., "..\\api\\dbFiles\\ttl\\globalGraph.ttl").
        globalGraph.write("..\\api\\dbFiles\\ttl\\globalGraph.ttl");

        // Print a message indicating that the global graph has been generated.
        System.out.println("+++++++++++++++++++++++++++++++++++++++ GLOBAL GRAPH GENERATED");

        // Generate the visual schema of the global graph and set it.
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        String newGraphicalSchema = visualLibInterface.generateVisualGraph(globalGraph);
        globalGraph.setGraphicalSchema(newGraphicalSchema);

        // Return the generated global graph.
        return globalGraph;
    }

    /**
     * Joins integration results to create a unified global graph based on the provided integrated graph and a list of join alignments.
     *
     * @param integratedGraph The integrated graph to which the join operation will be applied.
     * @param joinAlignments  The list of join alignments specifying how properties from different graphs should be joined.
     * @return The global graph resulting from the join operation.
     */
    public Graph joinIntegration(Graph integratedGraph, List<JoinAlignment> joinAlignments) {
        // Create an instance of the integration module.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();

        // Perform the join operation on the integrated graph using the provided join alignments.
        Graph globalGraph = integrationInterface.joinIntegration(integratedGraph, joinAlignments);

        // Generate the visual schema of the global graph and set it.
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        String newGraphicalSchema = visualLibInterface.generateVisualGraph(globalGraph);
        globalGraph.setGraphicalSchema(newGraphicalSchema);

        // Return the resulting global graph after the join operation.
        return globalGraph;
    }

    public Project addIntegratedDataset(String projectId, String id) {
        return projectService.addIntegratedDataset(projectId, id);
    }

    public List<Alignment> getAlignments(String projectId, String datasetId) throws SQLException, IOException, ClassNotFoundException {
        SourceService sourceService = new SourceService(appConfig, projectService, new RepositoryService(appConfig, projectService));
        Project project = getProject(projectId);

        Dataset datasetB = sourceService.getDatasetById(datasetId);
        Dataset datasetA = sourceService.getDatasetById(project.getIntegratedDatasets().get(0).getId());

        Graph graphA = project.getIntegratedGraph();

        Graph graphB = datasetB.getLocalGraph();

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // TODO review
        jdModuleInterface jdInterface = new jdModuleImpl(appConfig);

        List<Alignment> alignments = new ArrayList<>();
        alignments = jdInterface.getAlignments(datasetA, datasetB);

        List<Alignment> alignmentsWithFilter = new ArrayList<>();
        float minSimilarity = 0.3F;
        for (Alignment a: alignments) {
            System.out.println(a.getSimilarity());
            if(a.getSimilarity() >= minSimilarity) {
                a.setLabelA(a.getAttributeA().getName());
                a.setLabelB(a.getAttributeB().getName());
                a.setL(a.getAttributeA().getName()+"_"+a.getAttributeB().getName());
                a.setType("datatype");
                a.setIdentifier(true);
                a.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/"+datasetA.getId()+"/"+a.getAttributeA().getName());
                a.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/"+datasetB.getId()+"/"+a.getAttributeB().getName());
                alignmentsWithFilter.add(a);
            }
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return  alignmentsWithFilter;
    }
}
