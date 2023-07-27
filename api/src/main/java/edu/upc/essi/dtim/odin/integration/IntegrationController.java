package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GlobalGraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.GraphJenaImpl;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationData;
import edu.upc.essi.dtim.odin.integration.pojos.IntegrationTemporalResponse;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import edu.upc.essi.dtim.odin.project.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class IntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);

    private final IntegrationService integrationService;

    /**
     * Constructs a new instance of IntegrationController.
     *
     * @param integrationService the IntegrationService dependency for performing integration operations
     */
    IntegrationController(@Autowired IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping(value = "/project/{id}/integration")
    public ResponseEntity<IntegrationTemporalResponse> integrate(@PathVariable("id") String projectId,
                                                                 @RequestBody IntegrationData iData) {
        logger.info("INTEGRATING temporal with project: "+projectId);

        Project project = integrationService.getProject(projectId);

        //miramos si hay datasets suficientes a integrar en el proyecto
        if(project.getDatasets().size() > 1){
            //integramos la nueva fuente de datos sobre el grafo integrado existente y lo sobreescrivimos
            Graph integratedGraph = integrationService.integrateData(project.getIntegratedGraph(), iData.getDsB(), iData.getAlignments());

            String path = "..\\api\\dbFiles\\ttl\\";
            /*
            project.getIntegratedGraph().write(path+"graphA.ttl");
            iData.getDsB().getLocalGraph().write(path+"graphB.ttl");
            integratedGraph.write(path+"integrated.ttl");
             */

            Project projectToSave = integrationService.updateIntegratedGraphProject(project, integratedGraph);

            Graph globalGraph = integrationService.generateGlobalGraph(project.getIntegratedGraph(), iData.getDsB(), iData.getAlignments());
            projectToSave=integrationService.updateGlobalGraphProject(projectToSave, globalGraph);

            Project project1 = integrationService.saveProject(projectToSave);
            logger.info("PROJECT SAVED WITH THE NEW INTEGRATED GRAPH");
            Project project2 = integrationService.getProject(project1.getProjectId());

            List<JoinAlignment> joinProperties =  integrationService.generateJoinAlignments(project.getIntegratedGraph(), (Graph) iData.getDsB().getLocalGraph(), iData);

            return new ResponseEntity<>(new IntegrationTemporalResponse(project2, joinProperties), HttpStatus.OK);
        }
        //si no hay suficientes ERROR
        else{
            return new ResponseEntity<>(new IntegrationTemporalResponse(null,null), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/project/{id}/integration/join")
    public ResponseEntity<Project> integrateJoins(@PathVariable("id") String id, @RequestBody List<JoinAlignment> joinA){

        logger.info("INTEGRATING joins...");

        Project project = integrationService.getProject(id);

        Graph integratedSchema = integrationService.joinIntegration(project.getIntegratedGraph(), joinA);

        project.setIntegratedGraph((IntegratedGraphJenaImpl) integratedSchema);

        Graph globalSchema = integrationService.joinIntegration(project.getIntegratedGraph(), joinA);

        project.getIntegratedGraph().setGlobalGraph((GlobalGraphJenaImpl) globalSchema);


        Project savedProject = integrationService.saveProject(project);

        return new ResponseEntity(savedProject, HttpStatus.OK);
    }

    @PostMapping(value = "/project/{id}/integration/persist")
    public ResponseEntity<Project> acceptIntegration(@PathVariable("id") String id) {
        //todo: delete this call
        return new ResponseEntity(integrationService.getProject(id), HttpStatus.OK);
    }

    @PostMapping(value = "/project/{id}/datasources/persist")
    public ResponseEntity<Dataset> persistDataSource(@PathVariable("id") String id, @RequestBody Dataset dataSource) {
        //Dataset dataset = new Datase
        return new ResponseEntity<>(dataSource, HttpStatus.CREATED);
    }
}