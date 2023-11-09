package edu.upc.essi.dtim.odin.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.essi.dtim.odin.query.pojos.RDFSResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QueryService {
    public RDFSResult getQueryResult() {
        // Crear una instancia de RDFSResult
        RDFSResult res = new RDFSResult();

        // Configurar las columnas
        res.setColumns(Arrays.asList("Subject", "Predicate", "Object", "DataType"));

        // Filas hardcodeadas
        List<List<String>> hardCodedRows = Arrays.asList(
                Arrays.asList("Person1", "hasName", "John Doe", "xsd:string"),
                Arrays.asList("Person2", "hasAge", "25", "xsd:integer"),
                Arrays.asList("Person3", "hasOccupation", "Engineer", "xsd:string"),
                Arrays.asList("Person4", "hasCity", "New York", "xsd:string"),
                Arrays.asList("Person5", "hasAge", "30", "xsd:integer"),
                Arrays.asList("Person6", "hasOccupation", "Doctor", "xsd:string"),
                Arrays.asList("Person7", "hasCity", "San Francisco", "xsd:string"),
                Arrays.asList("Person8", "hasAge", "28", "xsd:integer"),
                Arrays.asList("Person9", "hasOccupation", "Teacher", "xsd:string"),
                Arrays.asList("Person10", "hasCity", "London", "xsd:string")
        );

        // Adaptar las filas al formato necesario
        List<String> adaptedRows = new ArrayList<>();

        for (List<String> rowData : hardCodedRows) {
            Map<String, String> rowMap = new HashMap<>();
            for (int i = 0; i < res.getColumns().size(); i++) {
                rowMap.put(res.getColumns().get(i), rowData.get(i));
            }
            // Convertir el mapa a una cadena JSON y agregarlo a la lista
            String rowJson = convertMapToJsonString(rowMap);
            adaptedRows.add(rowJson);
        }

        // Asignar las filas adaptadas a la instancia de RDFSResult
        res.setRows(adaptedRows);

        return res;
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
