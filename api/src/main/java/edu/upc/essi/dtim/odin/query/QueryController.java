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
    private QueryService Iservice;

    //    fromGraphicalToSPARQL
    @PostMapping(value="/query/{id}/graphical")
    public ResponseEntity<RDFSResult> queryFromGraphicalToSPARQL(@PathVariable("id") String id,
                                                                 @RequestBody QueryDataSelection body) {

        LOGGER.info("[POST /query/fromGraphicalToSPARQL/]");

        // Crear una instancia de RDFSResult
        RDFSResult res = new RDFSResult();

        // Configurar las columnas
        res.setColumns(Arrays.asList("Subject", "Predicate", "Object", "DataType"));

        // Configurar algunas filas de datos hardcodeados
        List<String> row1 = Arrays.asList("Person1", "hasName", "John Doe", "xsd:string");

        // Adaptar las filas al formato necesario
        List<String> adaptedRows = new ArrayList<>();

        Map<String, String> rowMap = new HashMap<>();
        for (int i = 0; i < res.getColumns().size(); i++) {
            rowMap.put(res.getColumns().get(i), row1.get(i));
        }

        // Convertir el mapa a una cadena JSON
        String rowJson = convertMapToJsonString(rowMap);

        adaptedRows.add(rowJson);

        // Asignar las filas adaptadas a la instancia de RDFSResult
        res.setRows(adaptedRows);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    // Método para convertir un mapa a una cadena JSON
    private String convertMapToJsonString(Map<String, String> map) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // Manejar la excepción según tus necesidades
            return null;
        }
    }

}
