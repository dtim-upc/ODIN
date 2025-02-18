package edu.upc.essi.dtim.odin.mappings;

import edu.upc.essi.dtim.NextiaCore.datasets.*;
import edu.upc.essi.dtim.NextiaCore.discovery.Attribute;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.NextiaCore.graph.MappingsGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.repositories.DataRepository;
import edu.upc.essi.dtim.NextiaCore.repositories.RelationalJDBCRepository;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
import edu.upc.essi.dtim.nextiamg.mappingsGeneration.MapgenResult;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.datasets.DatasetService;
import edu.upc.essi.dtim.odin.exception.ElementNotFoundException;
import edu.upc.essi.dtim.odin.exception.EmptyFileException;
import edu.upc.essi.dtim.odin.exception.FormatNotAcceptedException;
import edu.upc.essi.dtim.odin.exception.InternalServerErrorException;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS.bsModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaBS.bsModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaMG.mapgenModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaMG.mapgenModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDI.integrationModuleInterface;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerInterface;
import edu.upc.essi.dtim.odin.nextiaStore.graphStore.GraphStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.graphStore.GraphStoreInterface;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreFactory;
import edu.upc.essi.dtim.odin.nextiaStore.relationalStore.ORMStoreInterface;
import edu.upc.essi.dtim.odin.projects.ProjectService;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import edu.upc.essi.dtim.odin.repositories.RepositoryService;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static edu.upc.essi.dtim.odin.utils.Utils.generateUUID;
import static edu.upc.essi.dtim.odin.utils.Utils.reformatName;

@Service
public class MappingsService {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RestTemplate restTemplate;
    private final ORMStoreInterface ormDataResource = ORMStoreFactory.getInstance();


    /**
     * Manages the data from the dataset, executing the required preprocesses needed before the integration tasks. These
     * include bootstrapping to get the wrapper and the graph, generating the visual representation and storing the data.
     *
     *
     * @param projectID          Identification of the project to which the new dataset will be added.
     * @param mappingType        Type of mapping to be generated.
     */
    public Project genMappings(String mappingType, String projectID) {
        try {
            Project project = projectService.getProject(projectID);

            // Integrate the new data source onto the existing TEMPORAL integrated graph and overwrite it
            Graph integratedGraph = project.getIntegratedGraph();

            mapgenModuleInterface mgInterface = new mapgenModuleImpl();

            // Execute mapgeneration
            MapgenResult mgResult = mgInterface.generateMappings(mappingType, integratedGraph);
            MappingsGraph graphM =  mgResult.getGraph();

            // Set the mappings graph in the project.
            project.setMappingsGraph(graphM);
            project.getMappingsGraph().setGraphicalSchema(graphM.getGraphicalSchema());

            return project;

        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Error when generating mappings", e.getMessage());
        }
    }
}


