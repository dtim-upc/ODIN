package edu.upc.essi.dtim.odin.query;

import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleImpl;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR.qrModuleInterface;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.RDFSResult;
import org.springframework.stereotype.Service;


@Service
public class QueryService {
    public RDFSResult getQueryResult(QueryDataSelection body) {
        qrModuleInterface qrInterface = new qrModuleImpl();
        return qrInterface.makeQuery(body);
    }
}
