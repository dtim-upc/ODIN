package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.essi.dtim.odin.query.pojos.Property;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.RDFSResult;

import java.util.*;

public class qrModuleImpl implements qrModuleInterface{
    @Override
    public RDFSResult makeQuery(QueryDataSelection body) {
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

        List<Property> properties = body.getProperties();
        int rowCounter = properties.size();
        int hardcodedModule = hardCodedRows.size();
        for (Property property : properties) {
            Map<String, String> rowMap = new HashMap<>();
            for (int i = 0; i < res.getColumns().size(); i++) {
                if(i == 0) {
                    String iri = property.getIri();
                    String ultimaParte = iri;
                    // Encuentra la última posición del símbolo '#'
                    int lastHashIndex = iri.lastIndexOf("/");

                    // Comprueba si se encontró el símbolo '#'
                    if (lastHashIndex != -1) {
                        // Obtiene la parte de la cadena desde la última posición del símbolo '#' hasta el final
                        ultimaParte = iri.substring(lastHashIndex + 1);

                        // Imprime la última parte
                        System.out.println("Última parte de la cadena: " + ultimaParte);
                    } else {
                        // Si no se encuentra el símbolo '#', imprime un mensaje de error o realiza alguna acción apropiada
                        System.out.println("La cadena no contiene el símbolo '/'");
                    }

                    rowMap.put(res.getColumns().get(i), ultimaParte);
                }
                else {
                    rowMap.put(res.getColumns().get(i), hardCodedRows.get(rowCounter%hardcodedModule).get(i));
                }
            }

            --rowCounter;
            if (rowCounter < 0) rowCounter = 50;

            // Convertir el mapa a una cadena JSON y agregarlo a la lista
            String rowJson = convertMapToJsonString(rowMap);
            adaptedRows.add(rowJson);
        }

        // Asignar las filas adaptadas a la instancia de RDFSResult
        res.setRows(adaptedRows);

        return res;
    }

    // Método para convertir un mapa a una cadena JSON todo eliminar cuando ya no se hardcodee
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
