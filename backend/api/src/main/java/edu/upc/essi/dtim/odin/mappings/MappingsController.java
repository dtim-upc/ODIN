package edu.upc.essi.dtim.odin.mappings;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.odin.projects.pojo.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class MappingsController {
    private static final Logger logger = LoggerFactory.getLogger(MappingsController.class);
    @Autowired
    private MappingsService mappingsService;

    /**
     * Materializes a data product into a CSV file, mainly to be ingested by the intent generation pipeline.
     *
     * @param mappingType   The ID of the data product to be materialized
     * @return If the task was successful return a ResponseEntity with an OK HTTP code.
     */
    @PostMapping("/project/{projectID}/mappings/{mappingtype}/generate")
    public ResponseEntity<Project> generateMappings(@PathVariable("mappingtype") String mappingType,
                                                   @PathVariable("projectID") String projectID) {
        logger.info("generating mappings");
        Project savedProject = mappingsService.genMappings(mappingType, projectID);
        return new ResponseEntity<>(savedProject, HttpStatus.OK);
    }
}

