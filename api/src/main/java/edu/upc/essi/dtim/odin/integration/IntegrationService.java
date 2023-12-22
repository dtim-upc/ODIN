package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
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

@Service
public class IntegrationService {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private AppConfig appConfig;

    /**
     * Retrieves the domain of a property IRI from the specified graph.
     *
     * @param graph       The graph to query for the property's domain.
     * @param propertyIRI The IRI (Internationalized Resource Identifier) of the property.
     * @return The domain of the property if found, or null if not found.
     */
    private String getDomainOfProperty(Graph graph, String propertyIRI) {
        // Define a SPARQL query to retrieve the domain of the property and execute the query on the graph
        String query = "SELECT ?domain WHERE { <" + propertyIRI + "> <" + RDFS.domain.toString() + "> ?domain. }";
        List<Map<String, Object>> res = graph.query(query);

        // If the query result is not empty we extract and return the domain as a string. Otherwise, return null
        if (!res.isEmpty()) {
            return res.get(0).get("domain").toString();
        }
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
        // Define a SPARQL query to retrieve the RDFS label of the resource and execute the query on the graph
        String query = "SELECT ?label WHERE { <" + resourceIRI + "> <" + RDFS.label.toString() + "> ?label. }";
        List<Map<String, Object>> res = graph.query(query);

        // If the query result is not empty we extract and return the RDFS label as a string. Otherwise, return null
        if (!res.isEmpty()) {
            return res.get(0).get("label").toString();
        }
        return null;
    }

    public IntegrationTemporalResponse integrate(String projectId, IntegrationData iData) {
        Project project = projectService.getProject(projectId);

        // Integrate the new data source onto the existing TEMPORAL integrated graph and overwrite it
        Graph integratedGraph = integrateData(project.getIntegratedGraph(), iData.getDsB(), iData.getAlignments());
        Project projectToSave = updateTemporalIntegratedGraphProject(project, integratedGraph);

        // Add the new integrated dataset and save the project
        projectToSave = datasetService.addTemporalIntegratedDataset(projectToSave, iData.getDsB().getId());
        projectService.saveProject(projectToSave);

        // We get the project we have just stored with getProject to regenerate the global graphs (otherwise, the
        // visual representation will fail)
        Project savedProject = projectService.getProject(projectToSave.getProjectId());

        List<JoinAlignment> joinProperties = generateJoinAlignments(project.getIntegratedGraph(), iData.getDsB().getLocalGraph(), iData);

        return new IntegrationTemporalResponse(savedProject, joinProperties);
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

        // Retrieve the local graph of the dataset that we want to integrate
        String graphToIntegrateName = datasetToIntegrate.getLocalGraph().getGraphName(); // we only have the name
        Graph graphToIntegrate = graphStoreInterface.getGraph(graphToIntegrateName);
        datasetToIntegrate.setLocalGraph((LocalGraphJenaImpl) graphToIntegrate); // now we have the full graph; we need it for later

        // Integrate the data from graphB into the integratedGraph based on alignments; and generate the visual schema
        integrationModuleInterface integrationInterface = new integrationModuleImpl();
        Graph newIntegratedGraph = integrationInterface.integrate(integratedGraph, graphToIntegrate, alignments);
        newIntegratedGraph.setGraphicalSchema(generateVisualSchema(newIntegratedGraph));

        return newIntegratedGraph;
    }

    public Project updateTemporalIntegratedGraphProject(Project project, Graph integratedGraph) {
        // Create an integrated graph and assign the data from the new integrated graph
        // NECESSARY FOR CASTING STUFF
        IntegratedGraphJenaImpl integratedImpl = CoreGraphFactory.createIntegratedGraph();
        integratedImpl.setGraph(integratedGraph.getGraph());

        // Set the integrated graph in the project.
        project.setTemporalIntegratedGraph(integratedImpl);
        project.getTemporalIntegratedGraph().setGraphicalSchema(integratedGraph.getGraphicalSchema());

        return project;
    }

    private String generateVisualSchema(Graph graph) {
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        return visualLibInterface.generateVisualGraph(graph);
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
        // Get a list of unused alignments between graphA and graphB.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();
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

    public Project reviewJoins(String projectID, List<JoinAlignment> joinAlignments) {
        Project project = projectService.getProject(projectID);

        // Integrate the reviewed join alignments into the integrated graph and update the project
        Graph integratedSchema = joinIntegration(project.getTemporalIntegratedGraph(), joinAlignments);
        project = updateTemporalIntegratedGraphProject(project, integratedSchema);

        // Save and return the updated project (we use getProject to regenerate the global graphs)
        Project savedProject = projectService.saveProject(project);

        return projectService.getProject(savedProject.getProjectId());
    }

    /**
     * Joins integration results to create a unified global graph based on the provided integrated graph and a list of join alignments.
     *
     * @param integratedGraph The integrated graph to which the join operation will be applied.
     * @param joinAlignments  The list of join alignments specifying how properties from different graphs should be joined.
     * @return The global graph resulting from the join operation.
     */
    public Graph joinIntegration(Graph integratedGraph, List<JoinAlignment> joinAlignments) {
        // Perform the join operation on the integrated graph using the provided join alignments.
        integrationModuleInterface integrationInterface = new integrationModuleImpl();
        Graph joinGraph = integrationInterface.joinIntegration(integratedGraph, joinAlignments);

        // Generate the visual schema of the global graph and set it.
        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        String newGraphicalSchema = visualLibInterface.generateVisualGraph(joinGraph);
        joinGraph.setGraphicalSchema(newGraphicalSchema);

        // Return the resulting global graph after the join operation.
        return joinGraph;
    }

    public Project acceptIntegration(String projectID) {
        Project temporalProject = projectService.getProject(projectID);

        // Set the temporal integrated graph as the integrated graph
        Project projectToSave = updateIntegratedGraphProject(temporalProject, temporalProject.getTemporalIntegratedGraph());

        // Pass the newly integrated dataset from the temporalIntegratedDatasets to integratedDatasets and reset temporalIntegratedDatasets
        List<Dataset> temporalIntegratedDatasets = projectToSave.getTemporalIntegratedDatasets();
        String lastDatasetIdAdded = temporalIntegratedDatasets.get(temporalIntegratedDatasets.size()-1).getId();
        projectToSave = datasetService.addIntegratedDataset(projectToSave, lastDatasetIdAdded);
        projectToSave.setTemporalIntegratedDatasets(new ArrayList<>());

        projectService.saveProject(projectToSave);
        return projectService.getProject(projectID);
    }

    /**
     * Updates the integrated graph within a project with a new integrated graph.
     *
     * @param project         The project whose integrated graph is being updated.
     * @param temporalIntegratedGraph The new integrated graph to be set in the project.
     * @return The updated project with the new integrated graph.
     */
    public Project updateIntegratedGraphProject(Project project, Graph temporalIntegratedGraph) {
        // Create an instance of an integrated graph and assign the temporal graph's data
        IntegratedGraphJenaImpl integratedImpl = CoreGraphFactory.createIntegratedGraph();
        integratedImpl.setGraph(temporalIntegratedGraph.getGraph());

        // Set the integrated graph in the project.
        project.setIntegratedGraph(integratedImpl);
        project.getIntegratedGraph().setGraphicalSchema(temporalIntegratedGraph.getGraphicalSchema());

        return project;
    }

    public List<Alignment> getAlignments(String projectId, String datasetId) throws SQLException, IOException, ClassNotFoundException {
        Project project = projectService.getProject(projectId);
        Dataset datasetA = datasetService.getDatasetById(project.getIntegratedDatasets().get(0).getId());
        Dataset datasetB = datasetService.getDatasetById(datasetId);

        ////////////////////////////////////// TODO review
        jdModuleInterface jdInterface = new jdModuleImpl(appConfig);
        List<Alignment> alignments = jdInterface.getAlignments(datasetA, datasetB);

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
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return alignmentsWithFilter;
        //if (alignments.size() == 0) return new ResponseEntity(alignments, HttpStatus.NO_CONTENT); // throw exception
    }
}