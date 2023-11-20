package edu.upc.essi.dtim.odin.query;

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

@RestController
public class QueryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);


    @Autowired
    private QueryService queryService;

    //    fromGraphicalToSPARQL
    @PostMapping(value = "/query/{id}/graphical")
    public ResponseEntity<RDFSResult> queryFromGraphicalToSPARQL(@PathVariable("id") String id,
                                                                 @RequestBody QueryDataSelection body) {
        LOGGER.info("[POST /query/fromGraphicalToSPARQL/]");

        System.out.println(body.getGraphID());
        System.out.println(body.getGraphType());

        System.out.println("CLASS 0");
        System.out.println(body.getClasses().get(0).getIri());
        System.out.println(body.getClasses().get(0).getType());
        System.out.println(body.getClasses().get(0).getIsIntegrated());

        System.out.println("PROPERTY 0");
        System.out.println(body.getProperties().get(0).getDomain());
        System.out.println(body.getProperties().get(0).getRange());
        System.out.println(body.getProperties().get(0).getIri());
        System.out.println(body.getProperties().get(0).getIsIntegrated());
        System.out.println(body.getProperties().get(0).getType());


        RDFSResult res = queryService.getQueryResult(body, id);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
