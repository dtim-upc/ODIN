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
     * Generates mappings for a project and returns them as a downloadable TTL file.
     *
     * @param mappingType The type of mapping to generate.
     * @param projectID   The ID of the project.
     * @return ResponseEntity with the zip file containning the TTL files for download.
     */
    @GetMapping("/project/{projectID}/mappings/{mappingtype}/download")
    public ResponseEntity<ByteArrayResource> downloadMappings(
            @PathVariable("mappingtype") String mappingType,
            @PathVariable("projectID") String projectID) {

        logger.info("Downloading mappings for project: {}", projectID);
        return mappingsService.genMappings(mappingType, projectID);
    }
}

