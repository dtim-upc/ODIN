package edu.upc.essi.dtim.odin.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.RDFSResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class QueryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);


    @Autowired
    private QueryService queryService;

    //    fromGraphicalToSPARQL
    @PostMapping(value="/query/{id}/graphical")
    public ResponseEntity<RDFSResult> queryFromGraphicalToSPARQL(@PathVariable("id") String id,
                                                                 @RequestBody QueryDataSelection body) {

        LOGGER.info("[POST /query/fromGraphicalToSPARQL/]");

        RDFSResult res = queryService.getQueryResult();

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
