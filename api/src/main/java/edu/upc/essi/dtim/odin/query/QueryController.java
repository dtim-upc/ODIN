package edu.upc.essi.dtim.odin.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.essi.dtim.NextiaCore.queries.Workflow;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    @Autowired
    private QueryService queryService;

    // TODO: Description
    @PostMapping("/project/{projectID}/query-graph")
    public ResponseEntity<QueryResult> queryGraph(@PathVariable("projectID") String projectID,
                                                  @RequestBody QueryDataSelection body) {
        logger.info("Getting query");
        QueryResult res = queryService.getQueryResult(body, projectID);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
