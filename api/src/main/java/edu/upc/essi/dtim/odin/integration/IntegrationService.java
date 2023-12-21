package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationTemporalResponse;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.NextiaStore.GraphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.datasets.DatasetService;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationData;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD.jdModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaJD.jdModuleInterface;
import edu.upc.essi.dtim.odin.projects.Project;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.repositories.RepositoryService;
import javassist.compiler.ast.Pair;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DatasetService datasetService;
    private final RepositoryService repositoryService;
    private final AppConfig appConfig;

    /**
     * Constructor for the IntegrationService class.
     *
     * @param appConfig The application configuration.
     */
    public IntegrationService(@Autowired AppConfig appConfig, @Autowired RepositoryService repositoryService) {
        this.appConfig = appConfig;
        this.repositoryService = repositoryService;
    }

    public IntegrationTemporalResponse integrate(String projectID, IntegrationData iData) {
        Project project = projectService.getProject(projectID);

        // Count the total number of datasets within all repositories of the project. If there is only one, we can not integrate
        int totalDatasets = 0;
        for (DataRepository repository : project.getRepositories()) {
            totalDatasets += repository.getDatasets().size();
        }

        if (totalDatasets > 1) {
            // Integrate the new data source onto the existing integrated graph and add overwrite it in the project
            Graph integratedGraph = integrateData(project.getIntegratedGraph(), iData.getDsB(), iData.getAlignments());
            Project projectToSave = updateTemporalIntegratedGraphProject(project, integratedGraph);

            // Generate a new global graph and add it to the project
            Graph globalGraph = generateGlobalGraph(project.getIntegratedGraph());
            projectToSave = updateGlobalGraphProject(projectToSave, globalGraph);

            Project projectWithNewGraph = projectService.saveProject(projectToSave); // Project with new temporal integrated graph
            // Add the integrated dataset to the set of temporal integrated datasets
            projectWithNewGraph = projectService.addTemporalIntegratedDataset(projectWithNewGraph.getProjectId(), iData.getDsB().getId());

            List<JoinAlignment> joinProperties = generateJoinAlignments(projectWithNewGraph.getIntegratedGraph(), iData.getDsB().getLocalGraph(), iData);
            System.out.println(joinProperties);

            return new IntegrationTemporalResponse(projectWithNewGraph, joinProperties);
        } else {
            // If there are not enough datasets to integrate, return a bad request status
            throw new RuntimeException("Not enough datasets");
//            return new ResponseEntity<>(new IntegrationTemporalResponse(null, null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Integrates data from a second RDF graph into the provided integrated RDF graph based on specified alignments.
     *
     * @param integratedGraph The integrated RDF graph to which data will be integrated.
     * @param datasetToIntegrate             The dataset containing the second RDF graph.
     * @param alignments      A list of alignments specifying how the data should be integrated.
     * @return The integrated RDF graph with the integrated data.
     * @throws RuntimeException If there is an error while performing the integration.
     */
    public Graph integrateData(GraphJenaImpl integratedGraph, Dataset datasetToIntegrate, List<Alignment> alignments) {
        GraphStoreInterface graphStoreInterface;
        try {
            graphStoreInterface = GraphStoreFactory.getInstance(appConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Search in jenaFiles for the graph of the new dataset to integrate and assign it to the dataset
        String graphToIntegrateName = datasetToIntegrate.getLocalGraph().getGraphName();
        Graph graphToIntegrate = graphStoreInterface.getGraph(graphToIntegrateName);
        datasetToIntegrate.setLocalGraph((LocalGraphJenaImpl) graphToIntegrate);

        // Integrate the data from datasetToIntegrate into the integratedGraph based on the alignments.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();
        Graph newIntegratedGraph = integrationInterface.integrate(integratedGraph, graphToIntegrate, alignments);

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
        return projectService.getProject(projectId);
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
     * @param graphA The first graph for alignment comparison.
     * @param graphB The second graph for alignment comparison.
     * @param iData  The integration data containing alignments.
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
     * @param graph       The graph to query for the property's domain.
     * @param propertyIRI The IRI (Internationalized Resource Identifier) of the property.
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
     * @param graph       The graph to query for the resource's RDFS label.
     * @param resourceIRI The IRI (Internationalized Resource Identifier) of the resource.
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
     * @param project         The project whose integrated graph is being updated.
     * @param integratedGraph The new integrated graph to be set in the project.
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

    public Project updateTemporalIntegratedGraphProject(Project project, Graph integratedGraph) {
        // Create a new integrated graph and set its data with the incoming graph
        Graph integratedImpl = CoreGraphFactory.createIntegratedGraph();
        integratedImpl.setGraph(integratedGraph.getGraph());

        // Set the integrated graph in the project and its graphical representation
        project.setTemporalIntegratedGraph((IntegratedGraphJenaImpl) integratedImpl);
//        project.getTemporalIntegratedGraph().setGraphicalSchema(integratedGraph.getGraphicalSchema());

        return project;
    }

    /**
     * Updates the global graph within an integrated project with a new global graph.
     *
     * @param project     The project containing the integrated graph and the global graph to be updated.
     * @param globalGraph The new global graph to be set within the project's integrated graph.
     * @return The updated project with the new global graph.
     */
    public Project updateGlobalGraphProject(Project project, Graph globalGraph) {
        // Create a new global graph and set its data with the incoming graph
        Graph globalImpl = CoreGraphFactory.createGlobalGraph();
        globalImpl.setGraph(globalGraph.getGraph());

        // Set the global graph within the project's integrated graph and its graphical representation
        project.getIntegratedGraph().setGlobalGraph((GlobalGraphJenaImpl) globalImpl);
        project.getIntegratedGraph().getGlobalGraph().setGraphicalSchema(globalGraph.getGraphicalSchema());

        return project;
    }

    public Project updateTemporalGlobalGraphProject(Project project, Graph globalGraph) {
        // Create an instance of a global graph from the CoreGraphFactory.
        Graph globalImpl = CoreGraphFactory.createGlobalGraph();

        // Set the graph data of the global graph to the data from the provided globalGraph.
        globalImpl.setGraph(globalGraph.getGraph());

        // Set the global graph within the project's integrated graph.
        project.getTemporalIntegratedGraph().setGlobalGraph((GlobalGraphJenaImpl) globalImpl);

        // Set the graphical schema of the global graph within the project.
        project.getTemporalIntegratedGraph().getGlobalGraph().setGraphicalSchema(globalGraph.getGraphicalSchema());

        // Return the updated project with the new global graph.
        return project;
    }

    /**
     * Generates a global graph by integrating an integrated graph (integratedGraph) and a dataset (dsB)
     *
     * @param integratedGraph The integrated graph to which the global graph will be added.
     * @return The generated global graph resulting from the integration.
     * @throws RuntimeException If an error occurs during graph integration or when accessing the graph store.
     */
    public Graph generateGlobalGraph(GraphJenaImpl integratedGraph) {
        // Get the global graph from the new integrated graph
        integrationModuleInterface integrationInterface = new integrationModuleImpl();
        Graph globalGraph = integrationInterface.generateGlobalGraph(integratedGraph);

        System.out.println("+++++++++++++++++++++++++++++++++++++++ GLOBAL GRAPH GENERATED");

        // Generate the visual schema of the global graph and set it.
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        String newGraphicalSchema = visualLibInterface.generateVisualGraph(globalGraph);
        globalGraph.setGraphicalSchema(newGraphicalSchema);

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

    public List<Alignment> getAlignments(String projectID, String datasetToIntegrateID) throws SQLException, IOException, ClassNotFoundException {
        Project project = getProject(projectID);
        // TODO: This is wrong, because we are computing the alignments of the new dataset with ONLY the first of the
        // TODO: integrated datasets, when it should be all of them. Fix when we have the query algorithm done
        Dataset datasetA = datasetService.getDatasetById(project.getIntegratedDatasets().get(0).getId());
        Dataset datasetB = datasetService.getDatasetById(datasetToIntegrateID);

        jdModuleInterface jdInterface = new jdModuleImpl(appConfig);
        List<Alignment> alignments = jdInterface.getAlignments(datasetA, datasetB);

        // TODO: review Alignment class
        List<Alignment> alignmentsWithFilter = new ArrayList<>();
        float minSimilarity = 0.3F;
        for (Alignment a : alignments) {
            if (a.getSimilarity() >= minSimilarity) {
                a.setLabelA(a.getAttributeA().getName());
                a.setLabelB(a.getAttributeB().getName());
                a.setL(a.getAttributeA().getName() + "_" + a.getAttributeB().getName());
                a.setType("datatype");
                a.setIdentifier(true);
                a.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/" + datasetA.getId() + "/" + a.getAttributeA().getName());
                a.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/" + datasetB.getId() + "/" + a.getAttributeB().getName());
                alignmentsWithFilter.add(a);
            }
        }
        return alignmentsWithFilter;
    }
}
