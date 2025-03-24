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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static edu.upc.essi.dtim.odin.utils.Utils.generateUUID;
import static edu.upc.essi.dtim.odin.utils.Utils.reformatName;

@Service
public class MappingsService {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private AppConfig appConfig;

    /**
     * Generates mappings and provides them as a downloadable TTL file.
     *
     * @param mappingType Type of mapping to be generated.
     * @param projectID   Identification of the project.
     * @return ResponseEntity with the TTL file for download.
     */
    public ResponseEntity<ByteArrayResource> genMappings(String mappingType, String projectID) {
        try {
            Project project = projectService.getProject(projectID);

            // Retrieve the integrated graph for the project
            Graph integratedGraph = project.getIntegratedGraph();

            // Instantiate the mapping generation module
            mapgenModuleInterface mgInterface = new mapgenModuleImpl();

            // Instantiate the integration module
            integrationModuleInterface diInterface = new integrationModuleImpl();

            // Generate mappings
            MapgenResult mgResult = mgInterface.generateMappings(mappingType, integratedGraph);
            MappingsGraph graphM = mgResult.getGraph();

            // Convert RDF Graph to Turtle format
            StringWriter out = new StringWriter();
            RDFDataMgr.write(out, graphM.getGraph(), Lang.TURTLE);
            byte[] content = out.toString().getBytes();

            // Get global schema
            Model globalSchema = diInterface.generateGlobalGraph(integratedGraph).getGraph();

            // Convert RDF Graph to Turtle format
            StringWriter out2 = new StringWriter();
            RDFDataMgr.write(out2, globalSchema, Lang.TURTLE);
            byte[] content2 = out2.toString().getBytes();

            // Create a ZIP file containing both the TTL file and the global schema
            ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(zipOutputStream)) {
                // Add TTL mappings to the ZIP file
                zip.putNextEntry(new ZipEntry("mappings.ttl"));
                zip.write(content);
                zip.closeEntry();

                // Add global schema to the ZIP file
                zip.putNextEntry(new ZipEntry("generated_ontology.ttl"));
                zip.write(content2);
                zip.closeEntry();
            }

            byte[] zipContent = zipOutputStream.toByteArray();

            // Prepare the ZIP file for download
            ByteArrayResource resource = new ByteArrayResource(zipContent);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mappings_and_ontology.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Error when generating mappings", e.getMessage());
        }
    }
}


